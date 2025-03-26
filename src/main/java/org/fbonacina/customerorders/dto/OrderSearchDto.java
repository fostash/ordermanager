package org.fbonacina.customerorders.dto;

import java.time.LocalDate;
import java.util.List;
import org.fbonacina.customerorders.model.Order;

public record OrderSearchDto(
    Long id,
    String name,
    String description,
    LocalDate orderDate,
    List<String> productNames,
    int totalItems) {

  public static OrderSearchDto fromOrder(Order order) {
    List<String> names =
        order.getItems().stream().map(item -> item.getProduct().getName()).toList();

    return new OrderSearchDto(
        order.getId(),
        order.getName(),
        order.getDescription(),
        order.getOrderDate(),
        names,
        order.getItems().size());
  }
}
