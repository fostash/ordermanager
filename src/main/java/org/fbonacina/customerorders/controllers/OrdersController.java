package org.fbonacina.customerorders.controllers;

import java.time.LocalDate;
import java.util.List;
import org.fbonacina.customerorders.dto.AddProductDto;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderMessage;
import org.fbonacina.customerorders.services.MeilisearchService;
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
  private final MeilisearchService meilisearchService;

  public OrdersController(
      UserService userService, OrderService orderService, MeilisearchService meilisearchService) {
    this.userService = userService;
    this.orderService = orderService;
    this.meilisearchService = meilisearchService;
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
  public ResponseEntity<?> readUserOrder(@PathVariable Long userId) {
    try {
      return ResponseEntity.ok().body(orderService.readUserOrders(userId));
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError()
          .body("error reading orders for user %s. Error: %s".formatted(userId, e.getMessage()));
    }
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/user/{userId}")
  public ResponseEntity<String> createOrder(
      @PathVariable Long userId, @RequestBody NewOrderDto newOrder) {

    return userService
        .findById(userId)
        .map(user -> orderService.createOrder(newOrder, user))
        .map(
            order ->
                ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/v1/orders/{id}")
                            .buildAndExpand(order.getId())
                            .toUri())
                    .<String>build())
        .orElse(ResponseEntity.notFound().build());
  }

  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
    orderService.deleteOrder(orderId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/search")
  public List<OrderMessage> searchOrder(
      @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
      @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "ordername", required = false) String ordername) {
    return meilisearchService.search(dateFrom, dateTo, username, ordername);
    // return orderService.searchOrder(dateFrom, dateTo, username, ordername);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{orderId}/items")
  public ResponseEntity<?> addProductToOrder(
      @PathVariable Long orderId, @RequestBody AddProductDto product) {
    return orderService
        .addProduct(product.userId(), product.productId(), orderId, product.quantity())
        .map(orderItem -> ResponseEntity.accepted().body(orderItem))
        .orElse(ResponseEntity.notFound().build());
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{orderId}/items/{orderItemId}")
  public ResponseEntity<?> removeProductToOrder(
      @PathVariable Long orderId, @PathVariable Long orderItemId) {
    orderService.removeProduct(orderItemId);
    return ResponseEntity.noContent().build();
  }
}
