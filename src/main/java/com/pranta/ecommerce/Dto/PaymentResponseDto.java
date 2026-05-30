package com.pranta.ecommerce.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDto {
    private String status;
    private String sessionKey;
    private String gatewayUrl;
    private String message;
}
