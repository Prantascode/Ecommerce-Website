package com.pranta.ecommerce.Dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Integer rating;
    private String comment;
    private Integer helpfulCount;
    private Boolean verifiedPurchase;
    private LocalDateTime createdAt;
    private String customerName;
    private Long productId;
}
