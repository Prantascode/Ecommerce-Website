package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.BrandRequestDto;
import com.pranta.ecommerce.Dto.Response.BrandResponseDto;

public interface BrandService {
    
    BrandResponseDto createBrand(BrandRequestDto dto);
    
    List<BrandResponseDto> getAllBrands();
}