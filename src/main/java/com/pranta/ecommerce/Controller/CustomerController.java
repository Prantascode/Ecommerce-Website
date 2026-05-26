package com.pranta.ecommerce.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pranta.ecommerce.Dto.CustomerResponseDto;
import com.pranta.ecommerce.Service.CustomerService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/customer")
public class CustomerController {
    
    public final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponseDto> getMyProfile(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(customerService.getMyProfileDetails(email));
    }
}
