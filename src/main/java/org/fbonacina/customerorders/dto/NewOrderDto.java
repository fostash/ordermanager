package org.fbonacina.customerorders.dto;

import lombok.Builder;

@Builder
public record NewOrderDto(String name, String description) {}
