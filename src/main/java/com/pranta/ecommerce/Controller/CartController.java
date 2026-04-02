package com.pranta.ecommerce.Controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.CartItemRequestDto;
import com.pranta.ecommerce.Dto.CartItemResponseDto;
import com.pranta.ecommerce.Dto.CartResponseDto;
import com.pranta.ecommerce.Service.CartService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add")
    public ResponseEntity<CartItemResponseDto> addToCart(@Valid @RequestBody CartItemRequestDto itemRequestDto){
        return new ResponseEntity<>(cartService.addToCart(itemRequestDto),HttpStatus.CREATED);
    }
    
    @PreAuthorize("@userSecurity.isCurrentUserId(#userId, authenticatio.name)")
    @GetMapping("/userId/{userId}")
    public ResponseEntity<CartResponseDto> getCartByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PreAuthorize("@userSecurity.isCurrentUserId(#userId, authentication.name)")
    @PutMapping("/userId/{userId}/edit/{productId}")
    public ResponseEntity<CartItemResponseDto> updateCartItemQuantity(
        @PathVariable Long userId,
        @PathVariable Long productId,
        @RequestBody Map<String, Integer> UpdateQuantity) {
        
        Integer quantity = UpdateQuantity.get("quantity");
        return ResponseEntity.ok(cartService.updateCartItemQuantity(userId, productId, quantity));
        
    }

    @PreAuthorize("@cartSecurity.isCartOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> clearCart(@PathVariable Long id){
        cartService.clearCart(id);
        return ResponseEntity.ok("Item deleted");
    }
}
