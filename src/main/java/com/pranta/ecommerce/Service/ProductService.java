package com.pranta.ecommerce.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pranta.ecommerce.Dto.Request.ProductRequestDto;
import com.pranta.ecommerce.Dto.Response.ProductResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;

public interface ProductService {

    
    ProductResponseDto createProduct(ProductRequestDto dto);
    
    Page<ProductResponseDto> getAllProducts(Pageable pageable);
    
    ProductResponseDto getProductById(Long id);
    
    ProductResponseDto getProductByName(String name);
    
    Page<ProductResponseDto> getProductByPriceRange(double minPrice, double maxPrice,Pageable pageable);
    
    Page<ProductResponseDto> getProductByCategory(Category category,Pageable pageable);
    
    Page<ProductResponseDto> getProductByBrand(Brand brand,Pageable pageable);
    
    Page<ProductResponseDto> getProductsByColor(String color,Pageable pageable);
    
    Page<ProductResponseDto> getOutOfStockProducts(Pageable pageable);
    
    Page<ProductResponseDto> getDiscountedProducts(Pageable pageable);

    Page<ProductResponseDto> getAvailableProducts(Pageable pageable);
    
    ProductStockResponseDto getStockByProduct(Long productId);
    
    Page<ProductResponseDto> getStockLimitResponse(int threshold,Pageable pageable);
    
    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);
    
    ProductResponseDto updateStock(int quantity, Long productId);
    
    void deleteProduct(Long id);
}