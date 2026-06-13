package com.pranta.ecommerce.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.ReviewRequestDto;
import com.pranta.ecommerce.Dto.ReviewResponseDto;
import com.pranta.ecommerce.Dto.ReviewUpdateRequestDto;
import com.pranta.ecommerce.Service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{productId}")
    public ResponseEntity<ReviewResponseDto>  placeReview(Authentication authentication,@PathVariable Long productId,@Valid @RequestBody ReviewRequestDto requestDto){
        String email = authentication.getName();

        ReviewResponseDto response = reviewService.reviewOrderedProduct(email, productId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("edit/{reviewId}")
    public ResponseEntity<ReviewResponseDto> editTheReview(@PathVariable Long reviewId,@Valid @RequestBody ReviewUpdateRequestDto requestDto,Authentication authentication) {
       
        String email = authentication.getName();
        ReviewResponseDto response = reviewService.editReview(reviewId,email,requestDto);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteTheReview(Authentication authentication,@PathVariable Long reviewId){
        String email = authentication.getName();
        
        reviewService.deleteReview(reviewId, email);

        return ResponseEntity.ok("Review is deleted sucessfully");

    }
}
