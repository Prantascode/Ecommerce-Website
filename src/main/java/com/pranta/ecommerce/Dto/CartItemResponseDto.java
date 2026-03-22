package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal UnitPrice;
    private BigDecimal TotalPrice;
}
