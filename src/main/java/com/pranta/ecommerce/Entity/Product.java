package com.pranta.ecommerce.Entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is Required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description is Required")
    @Column(nullable = false)
    private String description;

    @NotBlank(message = "Price is Required")
    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Image url is Required")
    @Column(nullable = false)
    private String imageUrl;

    private Integer stock;

}
