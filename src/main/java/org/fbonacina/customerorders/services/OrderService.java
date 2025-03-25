package org.fbonacina.customerorders.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.fbonacina.customerorders.dto.NewOrder;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.model.User;

public interface OrderService {
  Long createOrder(NewOrder newOrder, User user);

  Optional<OrderItem> addProduct(Long userId, Long productId, Long orderId, int quantityRequested);

  Optional<Order> readOrderDetails(Long id);

  List<Order> readUserOrders(Long userId);

  Boolean removeProduct(Long orderItemId);

  List<Order> searchOrder(LocalDate dateFrom, LocalDate dateTo, String username, String ordername);
}
