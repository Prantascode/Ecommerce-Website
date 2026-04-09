package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;
import java.util.List;


import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemResponseDto> item;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;
}
