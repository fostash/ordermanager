package org.fbonacina.customerorders.services;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.dto.OrderDto;
import org.fbonacina.customerorders.dto.OrderProductDto;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.messages.OrderItemMessage;
import org.fbonacina.customerorders.messages.OrderMessage;
import org.fbonacina.customerorders.model.*;
import org.fbonacina.customerorders.pubsub.RedisOrderPublisher;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.OrderSpecification;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final RedisOrderPublisher redisOrderPublisher;

  @Autowired
  public OrderServiceImpl(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      OrderItemRepository orderItemRepository,
      RedisOrderPublisher redisOrderPublisher) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.orderItemRepository = orderItemRepository;
    this.redisOrderPublisher = redisOrderPublisher;
  }

  @Override
  public Optional<OrderDto> readOrderDetails(Long id) {
    return orderRepository
        .findById(id)
        .map(
            order ->
                OrderDto.builder()
                    .id(order.getId())
                    .name(order.getName())
                    .description(order.getDescription())
                    .orderDate(order.getOrderDate())
                    .products(
                        order.getItems().stream()
                            .map(
                                orderItem ->
                                    productRepository
                                        .findById(orderItem.getProductId())
                                        .map(
                                            product ->
                                                OrderProductDto.builder()
                                                    .id(product.getId())
                                                    .name(product.getName())
                                                    .description(product.getDescription())
                                                    .build())
                                        .orElseThrow(
                                            () ->
                                                new OrderException(
                                                    "product with id %s not found"
                                                        .formatted(orderItem.getProductId()),
                                                    HttpStatus.NOT_FOUND)))
                            .collect(toList()))
                    .build());
  }

  @Override
  public List<Order> readUserOrders(Long userId) {
    return orderRepository.findByUserId(userId);
  }

  @Transactional
  public Order createOrder(NewOrderDto newOrder, User user) {
    try {
      var order =
          orderRepository.save(
              Order.builder()
                  .name(newOrder.name())
                  .user(user)
                  .description(newOrder.description())
                  .orderDate(LocalDate.now())
                  .build());

      var orderMessage =
          OrderMessage.builder()
              .orderId(order.getId())
              .orderDescription(order.getDescription())
              .orderName(order.getName())
              .orderDate(order.getOrderDate().format(DateTimeFormatter.ISO_DATE))
              .username(user.getUsername())
              .build();

      log.debug("sync new order with meilisearch");
      redisOrderPublisher.publish(orderMessage);

      return order;
    } catch (Exception e) {
      throw new OrderException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  @Transactional
  public void deleteOrder(Long orderId) {
    orderRepository
        .findById(orderId)
        .ifPresent(
            order -> {
              order
                  .getItems()
                  .forEach(
                      orderItem -> {
                        productRepository
                            .findById(orderItem.getProductId())
                            .map(
                                product -> {
                                  product.setStockQuantity(
                                      product.getStockQuantity() + orderItem.getQuantity());
                                  return productRepository.save(product);
                                });
                        orderItemRepository.delete(orderItem);
                      });
              orderRepository.delete(order);
            });
  }

  @Transactional
  @Retryable(backoff = @Backoff(delay = 200))
  public Optional<OrderItem> addProduct(
      Long userId, Long productId, Long orderId, int quantityRequested) {
    return productRepository
        .findById(productId)
        .flatMap(
            product -> {
              if (product.getStockQuantity() < quantityRequested) {
                throw new OrderException("product.quantity-not-enough", HttpStatus.CONFLICT);
              }
              return orderRepository
                  .findByIdAndUserId(orderId, userId)
                  .map(
                      order -> {
                        var res =
                            orderItemRepository
                                .findByOrderIdAndProductId(order.getId(), product.getId())
                                .or(
                                    () ->
                                        Optional.of(
                                            OrderItem.builder()
                                                .order(order)
                                                .productId(product.getId())
                                                .quantity(0)
                                                .build()))
                                .map(
                                    orderItem -> {
                                      product.setStockQuantity(
                                          product.getStockQuantity() - quantityRequested);
                                      productRepository.save(product);
                                      orderItem.setQuantity(
                                          orderItem.getQuantity() + quantityRequested);
                                      return orderItemRepository.save(orderItem);
                                    });
                        var orderMessage =
                            OrderMessage.builder()
                                .orderId(order.getId())
                                .orderDescription(order.getDescription())
                                .orderName(order.getName())
                                .orderDate(order.getOrderDate().format(DateTimeFormatter.ISO_DATE))
                                .username(order.getUser().getUsername())
                                .items(
                                    order.getItems().stream()
                                        .map(
                                            orderItem ->
                                                OrderItemMessage.builder()
                                                    .productName(product.getName())
                                                    .quantity(orderItem.getQuantity())
                                                    .build())
                                        .collect(toList()))
                                .build();
                        log.debug("sync order with meilisearch");
                        redisOrderPublisher.publish(orderMessage);
                        return res;
                      })
                  .orElseThrow(
                      () ->
                          new OrderException(
                              "Order with id %s for user %s not found".formatted(orderId, userId),
                              HttpStatus.NOT_FOUND));
            });
  }

  @Override
  @Transactional
  public void removeProduct(Long orderItemId) {
    orderItemRepository
        .findById(orderItemId)
        .ifPresent(
            orderItem -> {
              productRepository
                  .findById(orderItem.getProductId())
                  .ifPresent(
                      product -> {
                        log.debug("update product quantity");
                        product.setStockQuantity(
                            product.getStockQuantity() + orderItem.getQuantity());
                        productRepository.save(product);
                      });

              log.debug("delete order item");
              var order = orderItem.getOrder();
              order.getItems().removeIf(item -> item.getId().compareTo(orderItemId) == 0);
              orderItemRepository.delete(orderItem);

              var orderMessage =
                  OrderMessage.builder()
                      .orderId(order.getId())
                      .orderDescription(order.getDescription())
                      .orderName(order.getName())
                      .orderDate(order.getOrderDate().format(DateTimeFormatter.ISO_DATE))
                      .username(order.getUser().getUsername())
                      .items(
                          order.getItems().stream()
                              .map(
                                  item ->
                                      productRepository
                                          .findById(item.getProductId())
                                          .map(
                                              product ->
                                                  OrderItemMessage.builder()
                                                      .productName(product.getName())
                                                      .quantity(item.getQuantity())
                                                      .build())
                                          .orElseThrow(
                                              () ->
                                                  new OrderException(
                                                      "product with id %s not found"
                                                          .formatted(item.getProductId()),
                                                      HttpStatus.NOT_FOUND)))
                              .collect(toList()))
                      .build();

              log.debug("sync order with meilisearch");
              redisOrderPublisher.publish(orderMessage);
            });
  }

  @Override
  public List<Order> searchOrder(
      LocalDate dateFrom, LocalDate dateTo, String username, String ordername) {
    Specification<Order> spec =
        OrderSpecification.orderDateBetween(dateFrom, dateTo)
            .and(OrderSpecification.filterOrderName(ordername))
            .and(OrderSpecification.filterUserName(username));
    return orderRepository.findAll(spec);
  }
}
