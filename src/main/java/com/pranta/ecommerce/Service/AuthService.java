package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.Request.LoginRequest;
import com.pranta.ecommerce.Dto.Request.RegistationDto;
import com.pranta.ecommerce.Dto.Response.AuthResponseDto;
import com.pranta.ecommerce.Dto.Response.Register_LoginResponseDto;

public interface AuthService {
    
    Register_LoginResponseDto register(RegistationDto dto);
    
    AuthResponseDto login(LoginRequest request);
    
    AuthResponseDto refreshToken(String requestRefreshToken);
    
    void logout(String email);
}