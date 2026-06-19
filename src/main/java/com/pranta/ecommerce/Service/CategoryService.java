package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.CategoryRequestDto;
import com.pranta.ecommerce.Dto.CategoryResponeDto;

public interface CategoryService {
    
    CategoryResponeDto createCategory(CategoryRequestDto dto);
    
    List<CategoryResponeDto> getCategorys();
}