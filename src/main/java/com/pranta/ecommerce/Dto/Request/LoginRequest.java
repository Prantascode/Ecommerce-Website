package com.pranta.ecommerce.Dto.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Email is Required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is Required")
    @Column(nullable = false)
    private String password;
}
