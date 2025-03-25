package org.fbonacina.customerorders.controllers;

import java.time.LocalDate;
import java.util.List;
import org.fbonacina.customerorders.dto.AddProduct;
import org.fbonacina.customerorders.dto.NewOrder;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderItem;
import org.fbonacina.customerorders.services.OrderService;
import org.fbonacina.customerorders.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

  private final UserService userService;
  private final OrderService orderService;

  public OrdersController(UserService userService, OrderService orderService) {
    this.userService = userService;
    this.orderService = orderService;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}")
  public ResponseEntity<Order> retrieveOrder(@PathVariable Long id) {
    return orderService
        .readOrderDetails(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/user/{userId}")
  public List<Order> readUserOrder(@PathVariable Long userId) {
    return orderService.readUserOrders(userId);
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/user/{userId}")
  public ResponseEntity<String> createOrder(
      @PathVariable Long userId, @RequestBody NewOrder newOrder) {

    return userService
        .findById(userId)
        .map(user -> orderService.createOrder(newOrder, user))
        .map(
            orderItemId ->
                ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/v1/orders/{id}")
                            .buildAndExpand(orderItemId)
                            .toUri())
                    .<String>build())
        .orElseThrow();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping()
  public List<Order> searchOrder(
      @RequestParam("dateFrom") LocalDate dateFrom,
      @RequestParam("dateTo") LocalDate dateTo,
      @RequestParam("username") String username,
      @RequestParam("ordername") String ordername) {
    return orderService.searchOrder(dateFrom, dateTo, username, ordername);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{orderId}/items")
  public ResponseEntity<OrderItem> addProductToOrder(
      @PathVariable Long orderId, @RequestBody AddProduct product) {
    return orderService
        .addProduct(product.userId(), product.productId(), orderId, product.quantity())
        .map(orderItem -> ResponseEntity.accepted().body(orderItem))
        .orElseThrow();
  }
}
