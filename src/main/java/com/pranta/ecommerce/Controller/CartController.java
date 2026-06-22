package com.pranta.ecommerce.Controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import com.pranta.ecommerce.Dto.Request.CartItemRequestDto;
import com.pranta.ecommerce.Dto.Response.CartItemResponseDto;
import com.pranta.ecommerce.Dto.Response.CartResponseDto;
import com.pranta.ecommerce.Service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(
    name = "Cart",
    description = "Cart Management API's"
)
public class CartController {
    
    private final CartService cartService;

    @Operation(
        summary = "Add to Cart",
        description = "User can Add to Cart products"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public ResponseEntity<CartItemResponseDto> addToCart(
            @Valid @RequestBody CartItemRequestDto itemRequestDto,
            Authentication authentication) {

        String email = authentication.getName();
        CartItemResponseDto response = cartService.addToCart(itemRequestDto, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get Cart",
        description = "User can get their Carts"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<CartResponseDto> getMyCart(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(cartService.getCartByUserEmail(email));
    }

    @Operation(
        summary = "Update Cart Items",
        description = "User can update Cart items"
    )
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/edit/{productId}")
    public ResponseEntity<CartItemResponseDto> updateCartItemQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> updateQuantity,
            Authentication authentication) {

        String email = authentication.getName();
        Integer quantity = updateQuantity.get("quantity");

        return ResponseEntity.ok(
            cartService.updateCartItemQuantity(email, productId, quantity)
        );
    }

    @Operation(
        summary = "Clear the Cart",
        description = "User can clear their Cart"
    )
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Authentication authentication) {
        String email = authentication.getName();
        cartService.clearCart(email);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}