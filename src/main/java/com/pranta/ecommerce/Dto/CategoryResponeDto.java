package com.pranta.ecommerce.Dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponeDto {
    
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;
}
