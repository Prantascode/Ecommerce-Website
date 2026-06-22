package com.pranta.ecommerce.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DiscountResponseDto {
    private Long id;
    private String name;
    private BigDecimal discountPercent;
    private Boolean active;
    private Boolean currentlyActive;   // computed from isDiscountCurrentlyActive()
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountStartDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime discountEndDate;
    
    private Long productId;
    private String productName;
    private BigDecimal discountedPrice;  // The price after applying this discount
}