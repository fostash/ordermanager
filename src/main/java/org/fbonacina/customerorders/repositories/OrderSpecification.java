package org.fbonacina.customerorders.repositories;

import java.time.LocalDate;
import org.fbonacina.customerorders.model.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

  public static Specification<Order> orderDateBetween(LocalDate start, LocalDate end) {
    return (root, query, cb) -> {
      if (start != null && end != null) {
        return cb.between(root.get("orderDate"), start, end);
      } else if (start != null) {
        return cb.greaterThanOrEqualTo(root.get("orderDate"), start);
      } else if (end != null) {
        return cb.lessThanOrEqualTo(root.get("orderDate"), end);
      } else {
        return null;
      }
    };
  }

  public static Specification<Order> filterOrderName(String orderName) {
    return (root, query, cb) -> {
      if (orderName != null && !orderName.isBlank()) {
        return cb.like(cb.lower(root.get("name")), orderName.toLowerCase() + "%");
      }
      return null;
    };
  }

  public static Specification<Order> filterUserName(String userName) {
    return (root, query, cb) -> {
      if (userName != null && !userName.isBlank()) {
        return cb.like(cb.lower(root.get("user").get("username")), userName.toLowerCase() + "%");
      }
      return null;
    };
  }
}
