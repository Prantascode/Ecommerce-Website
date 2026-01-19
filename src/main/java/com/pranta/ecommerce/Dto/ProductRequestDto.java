package com.pranta.ecommerce.Dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    
    private Long id;

    @NotBlank(message = "Name is Required")
    private String name;

    @NotBlank(message = "Description is Required")
    private String description;

    @NotBlank(message = "Price is Required")
    private BigDecimal price;

    @NotBlank(message = "Image url is Required")
    private String imageUrl;

    @NotNull
    private String stock;
}
