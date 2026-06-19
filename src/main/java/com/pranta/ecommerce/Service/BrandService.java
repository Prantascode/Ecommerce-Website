package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.BrandRequestDto;
import com.pranta.ecommerce.Dto.BrandResponseDto;

public interface BrandService {
    
    BrandResponseDto createBrand(BrandRequestDto dto);
    
    List<BrandResponseDto> getAllBrands();
}