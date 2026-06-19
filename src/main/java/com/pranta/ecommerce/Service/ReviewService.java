package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.ReviewRequestDto;
import com.pranta.ecommerce.Dto.ReviewResponseDto;
import com.pranta.ecommerce.Dto.ReviewUpdateRequestDto;

public interface ReviewService {
    
    ReviewResponseDto reviewOrderedProduct(String email, Long productId, ReviewRequestDto requestDto);
    
    ReviewResponseDto getOwnReview(String email, Long productId);
    
    List<ReviewResponseDto> getAllReview(Long productId);
    
    ReviewResponseDto editReview(Long productId, String email, ReviewUpdateRequestDto requestDto);
    
    void deleteReview(Long productId, String email);
}