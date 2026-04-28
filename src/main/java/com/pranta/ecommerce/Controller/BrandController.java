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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/brand")
public class BrandController {
    
    private final BrandService brandService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BrandResponseDto> createBrands(@Valid @RequestBody BrandRequestDto dto){
        return new ResponseEntity<>(
            brandService.createBrand(dto),
            HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<BrandResponseDto>> getBrands(){
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
