package com.pranta.ecommerce.Service.Impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Entity.RefreshToken;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.RefreshTokenRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refreshExpirationSec:604800}")
    private Long refreshTokenDurationSec;

    @Override
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Delete existing refresh token if present
        refreshTokenRepository.findByUser(user).ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            refreshTokenRepository.flush();
            log.info("Deleted existing refresh token for user: {}", email);
        });

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSec));
        
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Created new refresh token for user: {}", email);
        
        return savedToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            deleteRefreshToken(token);
            log.warn("Refresh token expired: {}", token.getToken());
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }
        log.debug("Refresh token verified successfully: {}", token.getToken());
        return token;
    }

    @Override
    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.info("Deleted refresh token: {}", refreshToken.getToken());
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(this::deleteRefreshToken);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(this::deleteRefreshToken);
    }
}