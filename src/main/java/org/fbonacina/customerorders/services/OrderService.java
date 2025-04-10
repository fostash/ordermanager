package org.fbonacina.customerorders.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.dto.OrderDto;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.model.User;

public interface OrderService {
  Order createOrder(NewOrderDto newOrder, User user);

  void deleteOrder(Long orderId);

  Optional<OrderItem> addProduct(Long userId, Long productId, Long orderId, int quantityRequested);

  Optional<OrderDto> readOrderDetails(Long id);

  List<Order> readUserOrders(Long userId);

  void removeProduct(Long orderItemId);

  List<Order> searchOrder(LocalDate dateFrom, LocalDate dateTo, String username, String ordername);
}
