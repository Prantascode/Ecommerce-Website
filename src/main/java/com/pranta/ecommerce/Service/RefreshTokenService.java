package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Entity.RefreshToken;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.RefreshTokenRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Configurable duration injected from properties, defaults to 7 days in seconds if not present
    @Value("${app.jwt.refreshExpirationSec:604800}")
    private Long refreshTokenDurationSec;

    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Try to find existing token first
       refreshTokenRepository.findByUser(user).ifPresent(existingToken -> {
        refreshTokenRepository.delete(existingToken);
        refreshTokenRepository.flush(); // Critical! Forces DELETE before INSERT
    });
        RefreshToken refreshToken = new RefreshToken();
        
        // Update the existing token or set new one
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSec));
        
        return refreshTokenRepository.save(refreshToken); // This will UPDATE if exists
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            deleteRefreshToken(token);
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }
        return token;
    }

    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(this::deleteRefreshToken);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(this::deleteRefreshToken);
    }
}