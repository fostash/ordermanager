package org.fbonacina.customerorders.dto;

import lombok.Builder;

@Builder
public record OrderProductDto(Long id, String name, String description, Integer quantity) {}
