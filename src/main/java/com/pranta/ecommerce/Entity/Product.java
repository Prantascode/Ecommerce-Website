package com.pranta.ecommerce.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Description is Required")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Price is Required")
    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Image url is Required")
    @Column(nullable = false)
    private String imageUrl;

    @PositiveOrZero
    private int stock;

    public boolean isAvailable(){
        return stock > 0;
    }

    @NotBlank(message = "Color is required")
    private String color;

    @ManyToOne
    @JoinColumn(name = "Category_id",nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "Brand_id",nullable = false)
    private Brand brand;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isDiscounted = false;

    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;

    public BigDecimal getDiscountedPrice() {
        if (isDiscountCurrentlyActive()) {
            BigDecimal savings = price.multiply(discountPercent.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP));
            return price.subtract(savings).setScale(2, BigDecimal.ROUND_HALF_UP); // Round to 2 decimal places
        }
        return price;
    }

    public boolean isDiscountCurrentlyActive() {
        if(!Boolean.TRUE.equals(isDiscounted) || discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();

        if(discountStartDate == null && discountEndDate == null) {
            return true; // Always active if no dates are set
        }

        if(discountEndDate == null) {
            return !now.isBefore(discountStartDate);
        }

        if(discountStartDate == null) {
            return now.isBefore(discountEndDate);
        }

        return !now.isBefore(discountStartDate) && now.isBefore(discountEndDate);
    }
}
