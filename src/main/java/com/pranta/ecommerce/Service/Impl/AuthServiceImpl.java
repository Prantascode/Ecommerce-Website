package com.pranta.ecommerce.Service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.LoginRequest;
import com.pranta.ecommerce.Dto.RegistationDto;
import com.pranta.ecommerce.Dto.Register_LoginResponseDto;
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
        log.info("Registering new user with email: {}", dto.getEmail());
        
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists: " + dto.getEmail());
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        Customer customer = new Customer();
        customer.setUser(savedUser);
        customerRepository.save(customer);
        log.info("Customer created for user ID: {}", savedUser.getId());

        return mapToDto(savedUser, customer);
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isActive()) {
            log.warn("Login attempt for inactive user: {}", request.getEmail());
            throw new InvalidRequestException("Your account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", request.getEmail());
            throw new InvalidRequestException("Invalid Credentials");
        }

        String accessToken = jwtutil.generateAccessToken(user.getEmail(), user.getRole().name());
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        Register_LoginResponseDto userDto;
        if (user.getRole() == User.Role.ADMIN) {
            userDto = mapAdminToDto(user);
        } else {
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + user.getEmail()));
            userDto = mapToDto(user, customer);
        }

        log.info("User logged in successfully: {}", request.getEmail());
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

                    Register_LoginResponseDto userDto;
                    if (user.getRole() == User.Role.ADMIN) {
                        userDto = mapAdminToDto(user);
                    } else {
                        Customer customer = customerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + user.getEmail()));
                        userDto = mapToDto(user, customer);
                    }

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

    private Register_LoginResponseDto mapToDto(User user, Customer customer) {
        Register_LoginResponseDto dto = new Register_LoginResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    private Register_LoginResponseDto mapAdminToDto(User user) {
        Register_LoginResponseDto dto = new Register_LoginResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }
}