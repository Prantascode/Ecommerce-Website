package com.pranta.ecommerce.Dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WishlistResponseDto {
    private Long id;

    private Long userId;
    private String username; 
    private String wishlistName;

    private Long productId;

    private String productName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;   
}
