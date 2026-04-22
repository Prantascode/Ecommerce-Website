package com.pranta.ecommerce.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CategoryRequestDto;
import com.pranta.ecommerce.Dto.CategoryResponeDto;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    public CategoryResponeDto createCategory(CategoryRequestDto dto){

        Category category = new Category();

        category.setName(dto.getName());

        Category categorys = categoryRepository.save(category);

        return mapToDto(categorys);

    }

    public List<CategoryResponeDto> getCategorys(){
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
                
    }

    private CategoryResponeDto mapToDto(Category category){
        return new CategoryResponeDto(
            category.getName()
        );
    }
}
