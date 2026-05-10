package com.pranta.ecommerce.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.LoginRequest;
import com.pranta.ecommerce.Dto.RegistationDto;
import com.pranta.ecommerce.Dto.Register_LoginResponseDto;
import com.pranta.ecommerce.Service.AuthService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Register_LoginResponseDto> register(@RequestBody RegistationDto dto){
        return new ResponseEntity<>(authService.register(dto),HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequest requestDto){
        return new ResponseEntity<>(authService.login(requestDto),HttpStatus.ACCEPTED);
    }
}
