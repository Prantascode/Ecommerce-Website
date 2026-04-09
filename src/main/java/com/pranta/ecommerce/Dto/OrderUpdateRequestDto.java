package com.pranta.ecommerce.Dto;

import com.pranta.ecommerce.Entity.Order.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateRequestDto {
    
    @NotNull(message = "New status is required")
    private OrderStatus status;
}
