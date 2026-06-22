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

import com.pranta.ecommerce.Dto.Request.CategoryRequestDto;
import com.pranta.ecommerce.Dto.Response.CategoryResponeDto;
import com.pranta.ecommerce.Service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/category")
@Tag(
    name = "Categories",
    description = "categories Management API's"
)
public class CategoryController {
    
    private final CategoryService categoryService;

    @Operation(
        summary = "Create Product Category",
        description = "Admin can create product category"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<CategoryResponeDto> createCategory(@Valid @RequestBody CategoryRequestDto dto){
        return new ResponseEntity<>(
            categoryService.createCategory(dto),
            HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get Product Categories",
        description = "Anyone can get product categorys"
    )
    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<CategoryResponeDto>> getCategorys(){
        return ResponseEntity.ok(categoryService.getCategorys());
    }
}
