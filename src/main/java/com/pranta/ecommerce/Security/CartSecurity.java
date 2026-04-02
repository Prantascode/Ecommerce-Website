package com.pranta.ecommerce.Security;

import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Component("cartSecurity")
@RequiredArgsConstructor
public class CartSecurity {
     private final CartRepository cartRepository;

    public boolean isCartOwner(Long cartId, String email) {
        return cartRepository.findById(cartId)
                .map(cart -> cart.getUser().getEmail().equals(email))
                .orElse(false);
    }
}
