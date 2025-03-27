package org.fbonacina.customerorders.messages;

import java.util.List;
import lombok.Builder;

@Builder
public record OrderMessage(
    Long orderId,
    String orderName,
    String orderDescription,
    String orderDate,
    String username,
    List<OrderItemMessage> items) {}
