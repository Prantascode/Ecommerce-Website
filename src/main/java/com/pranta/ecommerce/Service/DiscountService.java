package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.DiscountRequestDto;
import com.pranta.ecommerce.Dto.DiscountResponseDto;

public interface DiscountService {
    
    DiscountResponseDto createDiscount(DiscountRequestDto dto);
    
    DiscountResponseDto updateDiscount(Long id, DiscountRequestDto dto);
    
    DiscountResponseDto getDiscountById(Long id);
    
    List<DiscountResponseDto> getAllDiscounts();
    
    List<DiscountResponseDto> getDiscountsByProduct(Long productId);
    
    void deleteDiscount(Long id);
    
    DiscountResponseDto toggleDiscountStatus(Long id, boolean active);
    
    void syncDiscountActiveStatus();
}