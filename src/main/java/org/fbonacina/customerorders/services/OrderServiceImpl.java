package org.fbonacina.customerorders.services;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.model.*;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.OrderSpecification;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
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
  private final RedisTemplate<String, OrderMessage> orderMessageRedisTemplate;
  private final String ordersTopic;

  @Autowired
  public OrderServiceImpl(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      OrderItemRepository orderItemRepository,
      RedisTemplate<String, OrderMessage> orderMessageRedisTemplate,
      @Value("${meilisearch.ordersTopic}") String ordersTopic) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.orderItemRepository = orderItemRepository;
    this.orderMessageRedisTemplate = orderMessageRedisTemplate;
    this.ordersTopic = ordersTopic;
  }

  @Override
  public Optional<Order> readOrderDetails(Long id) {
    return orderRepository.findById(id);
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
              .creationDate(order.getOrderDate().format(DateTimeFormatter.ISO_DATE))
              .username(user.getUsername())
              .build();

      orderMessageRedisTemplate.convertAndSend(ordersTopic, orderMessage);

      return order;
    } catch (Exception e) {
      throw new OrderException(e.getMessage());
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
                      orderItem ->
                          productRepository
                              .findById(orderItem.getProductId())
                              .map(
                                  product -> {
                                    product.setStockQuantity(
                                        product.getStockQuantity() + orderItem.getQuantity());
                                    return productRepository.save(product);
                                  }));
              orderRepository.delete(order);
            });
  }

  @Transactional
  @Retryable(backoff = @Backoff(delay = 200))
  public Optional<OrderItem> addProduct(
      Long userId, Long productId, Long orderId, int quantityRequested) {
    var newOrderItem =
        productRepository
            .findById(productId)
            .flatMap(
                product -> {
                  if (product.getStockQuantity() < quantityRequested) {
                    throw new RuntimeException("product.quantity-not-enough");
                  }
                  return orderRepository
                      .findByIdAndUserId(orderId, userId)
                      .flatMap(
                          order -> {
                            var res =
                                orderItemRepository
                                    .findByOrderIdAndProductId(order.getId(), product.getId())
                                    .or(
                                        () ->
                                            Optional.of(
                                                OrderItem.builder()
                                                    .orderId(order.getId())
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
                                    .creationDate(
                                        order.getOrderDate().format(DateTimeFormatter.ISO_DATE))
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
                            orderMessageRedisTemplate.convertAndSend(ordersTopic, orderMessage);
                            return res;
                          });
                });

    return newOrderItem;
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
                  .map(
                      product -> {
                        product.setStockQuantity(
                            product.getStockQuantity() + orderItem.getQuantity());
                        productRepository.save(product);
                        return true;
                      });

              orderItemRepository.delete(orderItem);
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
