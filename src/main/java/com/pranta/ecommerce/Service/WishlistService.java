package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.WishlistRequestDto;
import com.pranta.ecommerce.Dto.Response.WishlistResponseDto;

public interface WishlistService {
    
    WishlistResponseDto createWishlist(String email, Long productId, WishlistRequestDto request);
    
    WishlistResponseDto getWishlistById(Long id, String email);
    
    List<WishlistResponseDto> getAllWishlists(String email);
    
    WishlistResponseDto updateWishlist(String email, Long productId, WishlistRequestDto request);
    
    String deleteWishlist(String email, Long id);
}