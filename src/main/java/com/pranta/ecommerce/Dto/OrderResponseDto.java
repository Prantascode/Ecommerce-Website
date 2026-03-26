package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;

    private String status;  

    private LocalDateTime orderDate;

    private List<OrderItemResponseDto> orderItems;
}
