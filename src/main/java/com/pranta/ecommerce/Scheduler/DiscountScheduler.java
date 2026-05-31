package com.pranta.ecommerce.Scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountScheduler {
    
    private final ProductRepository productRepository;

    @Scheduled(cron = "0 * * * * *") // Every minute
    @Transactional
    public void activateScheduledDiscounts(){
        LocalDateTime now = LocalDateTime.now();

        List<Product> toActivate = productRepository
                .findByIsDiscountedFalseAndDiscountStartDateBeforeAndDiscountEndDateAfter(now, now);

        for (Product product : toActivate) {
            product.setIsDiscounted(true);
            log.info("Auto-activated discount for product: {} ({})", product.getName(), product.getId());
        }

        productRepository.saveAll(toActivate);
    }

    @Scheduled(cron = "0 * * * * *") // Every minute
    @Transactional
    public void deactivateExpiredDiscounts(){
        LocalDateTime now = LocalDateTime.now();

        List<Product> toDeactivate = productRepository
                .findByIsDiscountedTrueAndDiscountEndDateBefore(now);

        for (Product product : toDeactivate) {
            product.setIsDiscounted(false);
            log.info("Auto-deactivated discount for product: {} ({})", product.getName(), product.getId());
        }

        productRepository.saveAll(toDeactivate);
    }
}
