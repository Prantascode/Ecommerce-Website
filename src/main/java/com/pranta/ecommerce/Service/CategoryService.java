package com.pranta.ecommerce.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.pranta.ecommerce.Dto.CategoryRequestDto;
import com.pranta.ecommerce.Dto.CategoryResponeDto;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    public CategoryResponeDto createCategory(@RequestBody CategoryRequestDto dto){

        Category category = new Category();

        category.setId(dto.getId());
        category.setName(dto.getName());

        Category categorys = categoryRepository.save(category);

        return mapToDto(categorys);

    }

    private CategoryResponeDto mapToDto(Category category){
        return new CategoryResponeDto(
            category.getId(),
            category.getName()
        );
    }
}
