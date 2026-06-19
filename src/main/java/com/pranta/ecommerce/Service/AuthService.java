package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.LoginRequest;
import com.pranta.ecommerce.Dto.RegistationDto;
import com.pranta.ecommerce.Dto.Register_LoginResponseDto;

public interface AuthService {
    
    Register_LoginResponseDto register(RegistationDto dto);
    
    AuthResponseDto login(LoginRequest request);
    
    AuthResponseDto refreshToken(String requestRefreshToken);
    
    void logout(String email);
}