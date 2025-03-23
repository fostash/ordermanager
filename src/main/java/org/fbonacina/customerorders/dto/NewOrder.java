package org.fbonacina.customerorders.dto;

import lombok.Builder;

@Builder
public record NewOrder(String name, String description) {}
