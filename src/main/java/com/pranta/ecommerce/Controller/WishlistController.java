package com.pranta.ecommerce.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.Request.WishlistRequestDto;
import com.pranta.ecommerce.Dto.Response.WishlistResponseDto;
import com.pranta.ecommerce.Service.WishlistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {
    
    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WishlistResponseDto> createWishlist(Authentication authentication,@Valid @RequestBody WishlistRequestDto request,@PathVariable Long productId){

        String email = authentication.getName();

        WishlistResponseDto responseDto = wishlistService.createWishlist(email,productId,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{wishlistId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WishlistResponseDto> getWishlist(Long wishlistId,Authentication authentication){
        String email = authentication.getName();

        WishlistResponseDto response = wishlistService.getWishlistById(wishlistId, email);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WishlistResponseDto>> getAllWishlists(Authentication authentication){
        String email = authentication.getName();

        List<WishlistResponseDto> response = wishlistService.getAllWishlists(email);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WishlistResponseDto> updateWishlist(
            @PathVariable Long productId,
            @RequestBody WishlistRequestDto request,
            Authentication authentication) {

        String email = authentication.getName();

        WishlistResponseDto response = wishlistService.updateWishlist(email, productId, request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<String> deleteWishlist(
            @PathVariable Long wishlistId,
            Authentication authentication) {

        String email = authentication.getName();

        String response = wishlistService.deleteWishlist(email, wishlistId);

        return ResponseEntity.ok(response);
    }

}
