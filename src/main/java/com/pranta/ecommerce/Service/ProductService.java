package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.ProductRequestDto;
import com.pranta.ecommerce.Dto.Response.ProductResponseDto;
import com.pranta.ecommerce.Dto.Response.ProductStockResponseDto;
import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;

public interface ProductService {
    
    ProductResponseDto createProduct(ProductRequestDto dto);
    
    List<ProductResponseDto> getAllProducts();
    
    ProductResponseDto getProductById(Long id);
    
    ProductResponseDto getProductByName(String name);
    
    List<ProductResponseDto> getProductByPriceRange(double minPrice, double maxPrice);
    
    List<ProductResponseDto> getProductByCategory(Category category);
    
    List<ProductResponseDto> getProductByBrand(Brand brand);
    
    List<ProductResponseDto> getProductsByColor(String color);
    
    List<ProductResponseDto> getOutOfStockProducts();
    
    List<ProductResponseDto> getDiscountedProducts();
    
    ProductStockResponseDto getStockByProduct(Long productId);
    
    List<ProductResponseDto> getStockLimitResponse(int threshold);
    
    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);
    
    ProductResponseDto updateStock(int quantity, Long productId);
    
    void deleteProduct(Long id);
}