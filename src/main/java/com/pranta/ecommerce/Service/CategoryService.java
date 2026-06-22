package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.CategoryRequestDto;
import com.pranta.ecommerce.Dto.Response.CategoryResponeDto;

public interface CategoryService {
    
    CategoryResponeDto createCategory(CategoryRequestDto dto);
    
    List<CategoryResponeDto> getCategorys();
}