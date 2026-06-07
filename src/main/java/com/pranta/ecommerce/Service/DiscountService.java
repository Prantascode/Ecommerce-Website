package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.DiscountRequestDto;
import com.pranta.ecommerce.Dto.DiscountResponseDto;
import com.pranta.ecommerce.Entity.Discount;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Repository.DiscountRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Transactional
    public DiscountResponseDto createDiscount(DiscountRequestDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

        Discount discount = new Discount();
        mapDtoToEntity(dto, discount, product);
        validateDiscountDates(discount);

        return toResponseDto(discountRepository.save(discount));
    }

    @Transactional
    public DiscountResponseDto updateDiscount(Long id, DiscountRequestDto dto) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

        boolean wasManuallyDisabled = !discount.getActive();
        mapDtoToEntity(dto, discount, product);
        
        if (wasManuallyDisabled && (dto.getActive() == null || !dto.getActive())) {
            discount.setActive(false);
        }
        
        validateDiscountDates(discount);
        return toResponseDto(discountRepository.save(discount));
    }

    @Transactional(readOnly = true)
    public DiscountResponseDto getDiscountById(Long id) {
        return discountRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<DiscountResponseDto> getAllDiscounts() {
        return discountRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiscountResponseDto> getDiscountsByProduct(Long productId) {
        return discountRepository.findByProductId(productId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new RuntimeException("Discount not found: " + id);
        }
        discountRepository.deleteById(id);
    }
    
    @Transactional
    public DiscountResponseDto toggleDiscountStatus(Long id, boolean active) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));
        
        discount.setActive(active);
        log.info("Discount {} manually {} by admin", id, active ? "activated" : "deactivated");
        
        return toResponseDto(discountRepository.save(discount));
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void syncDiscountActiveStatus() {
        List<Discount> expiredDiscounts = discountRepository.findExpiredActiveDiscounts();
        if (!expiredDiscounts.isEmpty()) {
            log.info("Deactivating {} expired discounts", expiredDiscounts.size());
            expiredDiscounts.forEach(discount -> {
                discount.setActive(false);
                log.debug("Discount '{}' (ID: {}) deactivated due to expiration", 
                         discount.getName(), discount.getId());
            });
            discountRepository.saveAll(expiredDiscounts);
        }
        
        List<Discount> pendingDiscounts = discountRepository.findPendingDiscountsToActivate();
        if (!pendingDiscounts.isEmpty()) {
            log.info("Activating {} pending discounts", pendingDiscounts.size());
            pendingDiscounts.forEach(discount -> {
                if (isDiscountValid(discount)) {
                    discount.setActive(true);
                    log.debug("Discount '{}' (ID: {}) activated as scheduled", 
                             discount.getName(), discount.getId());
                }
            });
            discountRepository.saveAll(pendingDiscounts);
        }
    }


    private void mapDtoToEntity(DiscountRequestDto dto, Discount discount, Product product) {
        discount.setName(dto.getName());
        discount.setDiscountPercent(dto.getDiscountPercent());
        discount.setProduct(product);
        discount.setDiscountStartDate(dto.getDiscountStartDate());
        discount.setDiscountEndDate(dto.getDiscountEndDate());
        
        if (dto.getActive() != null) {
            discount.setActive(dto.getActive());
        } else {
            discount.setActive(computeActive(discount, LocalDateTime.now()));
        }
    }

    private DiscountResponseDto toResponseDto(Discount discount) {
        DiscountResponseDto dto = new DiscountResponseDto();
        dto.setId(discount.getId());
        dto.setName(discount.getName());
        dto.setDiscountPercent(discount.getDiscountPercent());
        dto.setActive(discount.getActive());
        dto.setCurrentlyActive(discount.isDiscountCurrentlyActive());
        dto.setDiscountStartDate(discount.getDiscountStartDate());
        dto.setDiscountEndDate(discount.getDiscountEndDate());
        dto.setProductId(discount.getProduct() != null ? discount.getProduct().getId() : null);
        dto.setProductName(discount.getProduct() != null ? discount.getProduct().getName() : null);
        return dto;
    }

    private boolean computeActive(Discount d, LocalDateTime now) {
        if (!isDiscountValid(d)) {
            return false;
        }
        
        if (d.getDiscountStartDate() == null && d.getDiscountEndDate() == null) {
            return true;
        }
        
        if (d.getDiscountStartDate() == null) {
            return now.isBefore(d.getDiscountEndDate());
        }
        
        if (d.getDiscountEndDate() == null) {
            return !now.isBefore(d.getDiscountStartDate());
        }
        
        return !now.isBefore(d.getDiscountStartDate()) && now.isBefore(d.getDiscountEndDate());
    }
    
    private boolean isDiscountValid(Discount discount) {
        return discount.getDiscountPercent() != null 
               && discount.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0;
    }
    
    private void validateDiscountDates(Discount discount) {
        if (discount.getDiscountStartDate() != null && discount.getDiscountEndDate() != null) {
            if (discount.getDiscountStartDate().isAfter(discount.getDiscountEndDate())) {
                throw new RuntimeException("Discount start date must be before end date");
            }
        }
        
        if (discount.getDiscountPercent() != null) {
            if (discount.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("Discount percent cannot exceed 100%");
            }
            if (discount.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Discount percent cannot be negative");
            }
        }
    }
}