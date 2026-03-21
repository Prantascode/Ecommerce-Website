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
public class OrderRequestDto {
    private Long id;
    private Long userId;
    private Long orderItemId;
    private List<OrderItemResponseDto> item;
    private BigDecimal totalAmount;

    private String status;  

    private LocalDateTime createdAt;
}
