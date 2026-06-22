package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.ReviewRequestDto;
import com.pranta.ecommerce.Dto.Request.ReviewUpdateRequestDto;
import com.pranta.ecommerce.Dto.Response.ReviewResponseDto;

public interface ReviewService {
    
    ReviewResponseDto reviewOrderedProduct(String email, Long productId, ReviewRequestDto requestDto);
    
    ReviewResponseDto getOwnReview(String email, Long productId);
    
    List<ReviewResponseDto> getAllReview(Long productId);
    
    ReviewResponseDto editReview(Long productId, String email, ReviewUpdateRequestDto requestDto);
    
    void deleteReview(Long productId, String email);
}