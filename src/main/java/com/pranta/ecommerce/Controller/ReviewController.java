package com.pranta.ecommerce.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.ReviewRequestDto;
import com.pranta.ecommerce.Dto.ReviewResponseDto;
import com.pranta.ecommerce.Service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponseDto>  placeReview(Authentication authentication,@PathVariable Long productId,@Valid @RequestBody ReviewRequestDto requestDto){
        String email = authentication.getName();

        ReviewResponseDto response = reviewService.reviewOrderedProduct(email, productId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
