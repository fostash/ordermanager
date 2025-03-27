package org.fbonacina.customerorders.messages;

import lombok.Builder;

@Builder
public record OrderItemMessage(String productName, Integer quantity) {}
