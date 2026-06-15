package com.pranta.ecommerce.Dto;


import lombok.Data;

@Data
public class WishlistRequestDto {

    private String wishlistName;

    private Long productId;
}
