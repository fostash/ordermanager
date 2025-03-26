package org.fbonacina.customerorders.model;

import java.util.List;
import lombok.Builder;

@Builder
public record OrderMessage(
    Long orderId,
    String orderName,
    String orderDescription,
    String creationDate,
    String username,
    List<OrderItemMessage> items) {}
