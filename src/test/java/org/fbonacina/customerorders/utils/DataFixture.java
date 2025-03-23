package org.fbonacina.customerorders.utils;

import java.time.LocalDate;
import java.util.UUID;
import org.fbonacina.customerorders.model.Order;
import org.fbonacina.customerorders.model.OrderStatus;
import org.fbonacina.customerorders.model.Product;
import org.fbonacina.customerorders.model.User;

public interface DataFixture {

  default Product createProduct(int quantity) {
    var uuid = UUID.randomUUID().toString();
    return Product.builder()
        .name("prd-name-%s".formatted(uuid))
        .description("prd-descr-%s".formatted(uuid))
        .stockQuantity(quantity)
        .version(-1)
        .build();
  }

  default User createUser() {
    var uuid = UUID.randomUUID().toString();
    return User.builder()
        .firstName("firstname-%s".formatted(uuid))
        .lastName("lastname-%s".formatted(uuid))
        .email("mail-%s@test.it".formatted(uuid))
        .build();
  }

  default Order createOrder(User user) {
    var uuid = UUID.randomUUID().toString();
    return Order.builder()
        .orderDate(LocalDate.now())
        .userId(user.getId())
        .name("order-%s".formatted(uuid))
        .description("order-description-%s".formatted(uuid))
        .status(OrderStatus.NEW)
        .build();
  }
}
