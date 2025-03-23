package org.fbonacina.customerorders.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.fbonacina.customerorders.dto.NewOrder;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.model.User;
import org.fbonacina.customerorders.repositories.OrderItemRepository;
import org.fbonacina.customerorders.repositories.OrderRepository;
import org.fbonacina.customerorders.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Autowired
  public OrderServiceImpl(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      OrderItemRepository orderItemRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.orderItemRepository = orderItemRepository;
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
  public Long createOrder(NewOrder newOrder, User user) {
    try {
      var order =
          orderRepository.save(
              Order.builder()
                  .name(newOrder.name())
                  .userId(user.getId())
                  .description(newOrder.description())
                  .orderDate(LocalDate.now())
                  .build());
      return order.getId();
    } catch (Exception e) {
      throw new OrderException(e.getMessage());
    }
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
                throw new RuntimeException("product.quantity-not-enough");
              }
              return orderRepository
                  .findById(orderId)
                  .flatMap(
                      order ->
                          orderItemRepository
                              .findByOrderIdAndProductId(order.getId(), product.getId())
                              .or(
                                  () ->
                                      Optional.of(
                                          OrderItem.builder()
                                              .order(order)
                                              .product(product)
                                              .quantity(0L)
                                              .build()))
                              .map(
                                  orderItem -> {
                                    product.setStockQuantity(
                                        product.getStockQuantity() - quantityRequested);
                                    productRepository.save(product);
                                    orderItem.setQuantity(
                                        orderItem.getQuantity() + quantityRequested);
                                    return orderItemRepository.save(orderItem);
                                  }));
            });
  }
}
