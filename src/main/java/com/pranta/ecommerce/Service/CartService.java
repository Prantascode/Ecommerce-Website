package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.Request.CartItemRequestDto;
import com.pranta.ecommerce.Dto.Response.CartItemResponseDto;
import com.pranta.ecommerce.Dto.Response.CartResponseDto;
import com.pranta.ecommerce.Entity.Cart;

public interface CartService {
    
    CartItemResponseDto addToCart(CartItemRequestDto request, String email);
    
    CartResponseDto getCartByUserEmail(String email);
    
    CartItemResponseDto updateCartItemQuantity(String email, Long productId, Integer newQuantity);
    
    void removeItemFromCart(String email, Long productId);
    
    void clearCart(String email);
    
    CartRefreshResult refreshCart(Cart cart);
    
    void validateCartForOrder(Cart cart);
    
    Cart getCartAndRefresh(String email);
    
    class CartRefreshResult {
        private final boolean changed;
        private final int removedItemsCount;
        private final int priceUpdatedCount;
        private final java.math.BigDecimal newGrandTotal;
        
        public CartRefreshResult(boolean changed, int removedItemsCount, int priceUpdatedCount, java.math.BigDecimal newGrandTotal) {
            this.changed = changed;
            this.removedItemsCount = removedItemsCount;
            this.priceUpdatedCount = priceUpdatedCount;
            this.newGrandTotal = newGrandTotal;
        }
        
        public boolean isChanged() { return changed; }
        public int getRemovedItemsCount() { return removedItemsCount; }
        public int getPriceUpdatedCount() { return priceUpdatedCount; }
        public java.math.BigDecimal getNewGrandTotal() { return newGrandTotal; }
    }
}