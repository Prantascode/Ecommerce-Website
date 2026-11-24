package com.pranta.ecommerce.Service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.Request.LoginRequest;
import com.pranta.ecommerce.Dto.Request.RegistationDto;
import com.pranta.ecommerce.Dto.Response.AuthResponseDto;
import com.pranta.ecommerce.Dto.Response.Register_LoginResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.RefreshToken;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Security.Jwtutil;
import com.pranta.ecommerce.Service.AuthService;
import com.pranta.ecommerce.Service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final Jwtutil jwtutil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public Register_LoginResponseDto register(RegistationDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        log.info("Registering new user with email: {}", email);
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists: " + email);
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        Customer customer = new Customer();
        customer.setUser(savedUser);
        customerRepository.save(customer);
        log.info("Customer created for user ID: {}", savedUser.getId());

        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Login attempt for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            log.warn("Login attempt for inactive user: {}", email);
            throw new InvalidRequestException("Your account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", email);
            throw new InvalidRequestException("Invalid Credentials");
        }

        String accessToken = jwtutil.generateAccessToken(user.getEmail(), user.getRole().name());
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        Register_LoginResponseDto userDto = mapToDto(user);

        log.info("User logged in successfully: {}", email);
        return new AuthResponseDto(userDto, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(String requestRefreshToken) {
        log.info("Refreshing token");
        
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtutil.generateAccessToken(user.getEmail(), user.getRole().name());
                    Register_LoginResponseDto userDto = mapToDto(user);

                    log.info("Token refreshed successfully for user: {}", user.getEmail());
                    return new AuthResponseDto(userDto, newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token is missing or invalid."));
    }

    @Override
    @Transactional
    public void logout(String email) {
        log.info("Logging out user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        refreshTokenService.deleteByUser(user);
        log.info("User logged out successfully: {}", email);
    }

    private Register_LoginResponseDto mapToDto(User user) {
        Register_LoginResponseDto dto = new Register_LoginResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }
}