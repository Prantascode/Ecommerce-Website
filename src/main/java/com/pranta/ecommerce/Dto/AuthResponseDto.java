package com.pranta.ecommerce.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    
    private Register_LoginResponseDto user;
    private String token;
    private String refreshToken;
    
}
