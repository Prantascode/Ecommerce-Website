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
import lombok.Data;

@Entity
@Table(name = "discount")
@Data
public class Discount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    private Boolean active = true;

    private LocalDateTime discountStartDate;
    private LocalDateTime discountEndDate;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public boolean isDiscountCurrentlyActive() {
        if(!Boolean.TRUE.equals(active) || discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
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