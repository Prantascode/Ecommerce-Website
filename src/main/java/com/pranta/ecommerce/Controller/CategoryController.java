package com.pranta.ecommerce.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.CategoryRequestDto;
import com.pranta.ecommerce.Dto.CategoryResponeDto;
import com.pranta.ecommerce.Service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<CategoryResponeDto> createCategory(@Valid @RequestBody CategoryRequestDto dto){
        return new ResponseEntity<>(
            categoryService.createCategory(dto),
            HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<CategoryResponeDto>> getCategorys(){
        return ResponseEntity.ok(categoryService.getCategorys());
    }
}
