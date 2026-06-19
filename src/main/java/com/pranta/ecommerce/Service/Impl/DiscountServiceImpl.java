package com.pranta.ecommerce.Service.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pranta.ecommerce.Dto.DiscountRequestDto;
import com.pranta.ecommerce.Dto.DiscountResponseDto;
import com.pranta.ecommerce.Entity.Discount;
import com.pranta.ecommerce.Entity.Product;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.DiscountRepository;
import com.pranta.ecommerce.Repository.ProductRepository;
import com.pranta.ecommerce.Service.DiscountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public DiscountResponseDto createDiscount(DiscountRequestDto dto) {
        log.info("Creating discount for product ID: {}", dto.getProductId());
        
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        Discount discount = new Discount();
        mapDtoToEntity(dto, discount, product);
        validateDiscountDates(discount);

        Discount savedDiscount = discountRepository.save(discount);
        log.info("Discount created with ID: {}", savedDiscount.getId());
        
        return toResponseDto(savedDiscount);
    }

    @Override
    @Transactional
    public DiscountResponseDto updateDiscount(Long id, DiscountRequestDto dto) {
        log.info("Updating discount with ID: {}", id);
        
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        boolean wasManuallyDisabled = !discount.getActive();
        mapDtoToEntity(dto, discount, product);
        
        if (wasManuallyDisabled && (dto.getActive() == null || !dto.getActive())) {
            discount.setActive(false);
        }
        
        validateDiscountDates(discount);
        
        Discount updatedDiscount = discountRepository.save(discount);
        log.info("Discount updated with ID: {}", updatedDiscount.getId());
        
        return toResponseDto(updatedDiscount);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponseDto getDiscountById(Long id) {
        log.debug("Fetching discount with ID: {}", id);
        
        return discountRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponseDto> getAllDiscounts() {
        log.debug("Fetching all discounts");
        
        List<Discount> discounts = discountRepository.findAll();
        
        if (discounts.isEmpty()) {
            log.warn("No discounts found");
        }
        
        return discounts.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponseDto> getDiscountsByProduct(Long productId) {
        log.debug("Fetching discounts for product ID: {}", productId);
        
        List<Discount> discounts = discountRepository.findByProductId(productId);
        
        if (discounts.isEmpty()) {
            log.warn("No discounts found for product ID: {}", productId);
        }
        
        return discounts.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        log.info("Deleting discount with ID: {}", id);
        
        if (!discountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Discount not found with ID: " + id);
        }
        discountRepository.deleteById(id);
        log.info("Discount deleted with ID: {}", id);
    }
    
    @Override
    @Transactional
    public DiscountResponseDto toggleDiscountStatus(Long id, boolean active) {
        log.info("Toggling discount {} status to: {}", id, active);
        
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + id));
        
        discount.setActive(active);
        log.info("Discount {} manually {} by admin", id, active ? "activated" : "deactivated");
        
        return toResponseDto(discountRepository.save(discount));
    }

    @Override
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void syncDiscountActiveStatus() {
        log.debug("Running scheduled discount status sync");
        
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
                throw new InvalidRequestException("Discount start date must be before end date");
            }
        }
        
        if (discount.getDiscountPercent() != null) {
            if (discount.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidRequestException("Discount percent cannot exceed 100%");
            }
            if (discount.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidRequestException("Discount percent cannot be negative");
            }
        }
    }
}