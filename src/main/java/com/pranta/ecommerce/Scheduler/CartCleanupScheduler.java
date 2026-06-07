package com.pranta.ecommerce.Scheduler;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Entity.Cart;
import com.pranta.ecommerce.Repository.CartRepository;
import com.pranta.ecommerce.Service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CartCleanupScheduler {
    
    private final CartRepository cartRepository;
    private final CartService cartService;
    
    
    @Scheduled(cron = "0 0 * * * *")// Run every hour to refresh all active carts
    public void refreshAllActiveCarts() {
        log.info("Starting scheduled cart refresh");
        
        int refreshedCount = 0;
        int totalRemovedItems = 0;
        
        for (Cart cart : cartRepository.findAll()) {
            if (!cart.getItems().isEmpty()) {
                CartService.CartRefreshResult result = cartService.refreshCart(cart);
                if (result.isChanged()) {
                    refreshedCount++;
                    totalRemovedItems += result.getRemovedItemsCount();
                }
            }
        }
        
        log.info("Scheduled cart refresh completed. Refreshed {} carts, removed {} items total", 
            refreshedCount, totalRemovedItems);
    }
}
