package org.fbonacina.customerorders.dto;

import lombok.Builder;

@Builder
public record AddProduct(Long userId, Long productId, Integer quantity) {}
