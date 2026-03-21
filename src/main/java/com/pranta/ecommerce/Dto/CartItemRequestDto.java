package com.pranta.ecommerce.Dto;

import lombok.Data;

@Data
public class CartItemRequestDto {
    private Long userId;
    private Long productId;
    private Integer quantity;
}
