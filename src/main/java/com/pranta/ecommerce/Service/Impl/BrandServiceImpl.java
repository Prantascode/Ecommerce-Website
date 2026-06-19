package com.pranta.ecommerce.Service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.BrandRequestDto;
import com.pranta.ecommerce.Dto.BrandResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Repository.BrandRepository;
import com.pranta.ecommerce.Service.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrandServiceImpl implements BrandService {
    
    private final BrandRepository brandRepository;

    @Override
    public BrandResponseDto createBrand(BrandRequestDto dto) {
        log.info("Creating brand: {}", dto.getName());
        
        String brandName = dto.getName().trim();
        
        if (brandRepository.findByName(brandName).isPresent()) {
            throw new DuplicateResourceException("Brand already exists with name: " + brandName);
        }

        Brand brand = new Brand();
        brand.setName(brandName);

        Brand brandSaved = brandRepository.save(brand);
        log.info("Brand created with ID: {}", brandSaved.getId());
        
        return mapToDto(brandSaved);
    }

    @Override
    public List<BrandResponseDto> getAllBrands() {
        log.debug("Fetching all brands");
        
        List<Brand> brands = brandRepository.findAll();

        if (brands.isEmpty()) {
            log.warn("No brands found");
            return Collections.emptyList();
        }
        
        return brands.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BrandResponseDto mapToDto(Brand brand) {
        return new BrandResponseDto(
            brand.getName()
        );
    }
}