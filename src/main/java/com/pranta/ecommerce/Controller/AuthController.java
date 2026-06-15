package com.pranta.ecommerce.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.LoginRequest;
import com.pranta.ecommerce.Dto.RefreshTokenRequest;
import com.pranta.ecommerce.Dto.RegistationDto;
import com.pranta.ecommerce.Dto.Register_LoginResponseDto;
import com.pranta.ecommerce.Service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication",
    description = "User Authentication API's"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "User Registration",
        description = "User register themself as a USER"
    )
    @Transactional
    @PostMapping("/register")
    public ResponseEntity<Register_LoginResponseDto> register(@RequestBody RegistationDto dto){
        return new ResponseEntity<>(authService.register(dto), HttpStatus.CREATED);
    }

    @Operation(
        summary = "User Login",
        description = "User can login via email and password"
    )
    @Transactional
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "Refresh Access Token",
        description = "Provides a brand new access token using a valid, non-expired refresh token"
    )
    @Transactional
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(
        summary = "User Logout",
        description = "Logs out the current user by destroying their database refresh token session"
    )
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Principal principal) {
        if (principal == null) {
            throw new com.pranta.ecommerce.Exceptions.InvalidRequestException("No active session found");
        }
        authService.logout(principal.getName());
        return ResponseEntity.ok("Logged out successfully!");
    }
}