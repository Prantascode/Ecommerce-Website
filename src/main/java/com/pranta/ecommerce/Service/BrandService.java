package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Repository.BrandRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.BrandRequestDto;
import com.pranta.ecommerce.Dto.BrandResponseDto;
import com.pranta.ecommerce.Entity.Brand;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BrandService {
    
    private final BrandRepository brandRepository;


    public BrandResponseDto createBrand(BrandRequestDto dto){

        Brand brand = new Brand();
        brand.setName(dto.getName());

        Brand brandSaved = brandRepository.save(brand);
        
        return mapToDto(brandSaved);
    }

    public List<BrandResponseDto> getAllBrands(){
        List<Brand> brand = brandRepository.findAll();

        if (brand.isEmpty()) {
            return Collections.emptyList();
        }
        return brand
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private BrandResponseDto mapToDto(Brand brand){
        return new BrandResponseDto(
            brand.getName()
        );
    }

}
