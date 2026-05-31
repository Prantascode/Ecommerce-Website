package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String imageUrl;
    private int stock;
    private String color;
    private boolean isAvailable;
    private Category category;
    private Brand brand;

    private BigDecimal discountPercent;
    private Boolean isDiscounted;
    private Boolean isDiscountActive;    // reflects real-time status
    private BigDecimal discountedPrice;
    private BigDecimal savedAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountEndDate;
}
