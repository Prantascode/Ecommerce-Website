package com.pranta.ecommerce.Service;

import java.util.Optional;

import com.pranta.ecommerce.Entity.RefreshToken;
import com.pranta.ecommerce.Entity.User;

public interface RefreshTokenService {
    
    RefreshToken createRefreshToken(String email);
    
    Optional<RefreshToken> findByToken(String token);
    
    RefreshToken verifyExpiration(RefreshToken token);
    
    void deleteRefreshToken(RefreshToken refreshToken);
    
    void deleteByToken(String token);
    
    void deleteByUser(User user);
}