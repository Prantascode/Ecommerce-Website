package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;

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
    private String imageUrl;
    private int stock;
    private boolean isAvailable;
    private Category category;

}
