package org.fbonacina.customerorders.controllers;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.fbonacina.customerorders.dto.AddProductDto;
import org.fbonacina.customerorders.dto.NewOrderDto;
import org.fbonacina.customerorders.dto.OrderDto;
import org.fbonacina.customerorders.exceptions.OrderException;
import org.fbonacina.customerorders.messages.OrderMessage;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.services.MeilisearchService;
import org.fbonacina.customerorders.services.OrderService;
import org.fbonacina.customerorders.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
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
  public ResponseEntity<OrderDto> retrieveOrder(@PathVariable Long id) {

    log.debug("retrieve order details for order id {}", id);
    return orderService
        .readOrderDetails(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/user/{userId}")
  public ResponseEntity<?> readUserOrder(@PathVariable Long userId) {
    log.debug("retrieve orders for user with id {}", userId);
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

    log.debug("create new order for user with id {}", userId);
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
        .orElseThrow(() -> new OrderException("User not found", HttpStatus.NOT_FOUND));
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{orderId}")
  public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
    try {
      orderService.deleteOrder(orderId);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      throw new OrderException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/dbsearch")
  public List<Order> dbsearchOrder(
      @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
      @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "ordername", required = false) String ordername) {
    try {
      return orderService.searchOrder(dateFrom, dateTo, username, ordername);
    } catch (RuntimeException e) {
      throw new OrderException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/meilisearch")
  public List<OrderMessage> meilisearchOrder(
      @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
      @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "ordername", required = false) String ordername) {
    try {
      return meilisearchService.search(dateFrom, dateTo, username, ordername);
    } catch (RuntimeException e) {
      throw new OrderException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{orderId}/items")
  public ResponseEntity<?> addProductToOrder(
      @PathVariable Long orderId, @RequestBody AddProductDto product) {
    return orderService
        .addProduct(product.userId(), product.productId(), orderId, product.quantity())
        .map(orderItem -> ResponseEntity.accepted().body(orderItem))
        .orElseThrow(
            () ->
                new OrderException(
                    "product with id %s not found".formatted(product.productId()),
                    HttpStatus.NOT_FOUND));
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{orderId}/items/{orderItemId}")
  public ResponseEntity<?> removeProductToOrder(
      @PathVariable Long orderId, @PathVariable Long orderItemId) {
    orderService.removeProduct(orderItemId);
    return ResponseEntity.noContent().build();
  }
}
