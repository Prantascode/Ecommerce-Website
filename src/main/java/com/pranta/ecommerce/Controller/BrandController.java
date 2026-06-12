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

import com.pranta.ecommerce.Dto.BrandRequestDto;
import com.pranta.ecommerce.Dto.BrandResponseDto;
import com.pranta.ecommerce.Service.BrandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/brand")
@Tag(
    name = "Brand",
    description = "Brand Management API's"
)
public class BrandController {
    
    private final BrandService brandService;

    @Operation(
        summary = "Create Brand",
        description = "Admin can create a brand for product"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BrandResponseDto> createBrands(@Valid @RequestBody BrandRequestDto dto){
        return new ResponseEntity<>(
            brandService.createBrand(dto),
            HttpStatus.CREATED
        );
    }

    @Operation(
        summary = "Get Brand",
        description = "Anyone can get brand"
    )
    //@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<BrandResponseDto>> getBrands(){
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
