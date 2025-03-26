package org.fbonacina.customerorders.model;

import lombok.Builder;

@Builder
public record OrderItemMessage(String productName, Integer quantity) {}
