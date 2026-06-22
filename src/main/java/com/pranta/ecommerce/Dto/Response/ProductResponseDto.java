package com.pranta.ecommerce.Dto.Response;

import java.math.BigDecimal;
import java.util.List;

import com.pranta.ecommerce.Entity.Brand;
import com.pranta.ecommerce.Entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice; 
    private String imageUrl;
    private int stock;
    private String color;
    private boolean isAvailable;
    private Category category;
    private Brand brand;
    private List<DiscountResponseDto> discounts;  
    private DiscountResponseDto activeDiscount;   
    private boolean hasActiveDiscount;   
    private Double averageRating;
    private int totalReviews;         
}