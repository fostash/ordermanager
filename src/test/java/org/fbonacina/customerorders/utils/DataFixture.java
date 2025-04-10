package org.fbonacina.customerorders.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.fbonacina.customerorders.model.*;

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
        .username("username-%s".formatted(uuid))
        .password("password")
        .role("user")
        .build();
  }

  default Order createOrder(User user) {
    return createOrder(user, LocalDate.now(), "order");
  }

  default Order createOrder(User user, LocalDate orderDate, String searchBy) {
    var uuid = UUID.randomUUID().toString();
    return Order.builder()
        .orderDate(orderDate)
        .user(user)
        .name("%s-%s".formatted(searchBy, uuid))
        .description("order-description-%s".formatted(uuid))
        .build();
  }

  default OrderItem createOrderItem(Product product, Order order, int quantity) {
    return OrderItem.builder()
        .productId(product.getId())
        .orderId(order.getId())
        .quantity(quantity)
        .build();
  }

  default String asJsonString(final Object obj) {
    try {
      return new ObjectMapper()
          .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
          .writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
