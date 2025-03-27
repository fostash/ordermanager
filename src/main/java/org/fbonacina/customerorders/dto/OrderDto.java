package org.fbonacina.customerorders.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.fbonacina.customerorders.model.User;

@Builder
public record OrderDto(
    Long id,
    String name,
    String description,
    User user,
    LocalDate orderDate,
    List<OrderProductDto> products) {}
