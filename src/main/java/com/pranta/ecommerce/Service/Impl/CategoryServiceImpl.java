package com.pranta.ecommerce.Service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CategoryRequestDto;
import com.pranta.ecommerce.Dto.CategoryResponeDto;
import com.pranta.ecommerce.Entity.Category;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Repository.CategoryRepository;
import com.pranta.ecommerce.Service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponeDto createCategory(CategoryRequestDto dto) {
        log.info("Creating category: {}", dto.getName());
        
        String categoryName = dto.getName().trim();
        
        if (categoryRepository.findByName(categoryName).isPresent()) {
            throw new DuplicateResourceException("Category already exists with name: " + categoryName);
        }

        Category category = new Category();
        category.setName(categoryName);

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created with ID: {}", savedCategory.getId());

        return mapToDto(savedCategory);
    }

    @Override
    public List<CategoryResponeDto> getCategorys() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            log.warn("No categories found");
            return Collections.emptyList();
        }
        
        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CategoryResponeDto mapToDto(Category category) {
        return new CategoryResponeDto(
            category.getName()
        );
    }
}