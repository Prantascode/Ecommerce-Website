package com.pranta.ecommerce.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OderItemRequestDto {
    @NotNull
    private Long orderId;

    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;
}

