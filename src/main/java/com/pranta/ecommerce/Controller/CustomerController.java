package com.pranta.ecommerce.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.pranta.ecommerce.Dto.CustomerRequestDto;
import com.pranta.ecommerce.Dto.CustomerResponseDto;
import com.pranta.ecommerce.Service.CustomerService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customers")
@Tag(
    name = "Customers",
    description = "Customers Management API's"
)
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<CustomerResponseDto> getMyProfileDetails(Authentication authentication) {

        String email = authentication.getName();

        CustomerResponseDto response = customerService.getMyProfileDetails(email);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/me")
    public ResponseEntity<CustomerResponseDto> updateMyProfileDetails(
            Authentication authentication,
            @Valid @RequestBody CustomerRequestDto dto
    ) {
        String email = authentication.getName();

        CustomerResponseDto response = customerService.updateMyProfileDetails(email, dto);

        return ResponseEntity.ok(response);
    }
}