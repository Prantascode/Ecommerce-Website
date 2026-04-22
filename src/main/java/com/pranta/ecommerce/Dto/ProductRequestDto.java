package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    
    private Long id;

    @NotBlank(message = "Name is Required")
    @Column(nullable = false,unique = true)
    private String name;

    @NotBlank(message = "Description is Required")
    private String description;

    @NotNull(message = "Price is Required")
    private BigDecimal price;

    @NotBlank(message = "Image url is Required")
    private String imageUrl;

    @PositiveOrZero
    private int stock;

    @NotNull(message = "Category is Required")
    private Long category_id;
}
