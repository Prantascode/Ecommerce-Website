package com.pranta.ecommerce.Entity;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    //private BigDecimal discountedPrice;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Category_id",nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Brand_id",nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "product")
    @BatchSize(size = 25)
    private List<Discount> discounts;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    private int totalReviews;
}
