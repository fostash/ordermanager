package org.fbonacina.customerorders.dto;

import lombok.Builder;

@Builder
public record AddProductDto(Long userId, Long productId, Integer quantity) {}
