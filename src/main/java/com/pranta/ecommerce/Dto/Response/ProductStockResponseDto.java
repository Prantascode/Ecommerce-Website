package com.pranta.ecommerce.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockResponseDto {
    private Long productId;
    private String productName;
    private Integer Stock;
}
