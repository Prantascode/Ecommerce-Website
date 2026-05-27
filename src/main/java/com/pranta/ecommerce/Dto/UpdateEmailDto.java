package com.pranta.ecommerce.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEmailDto {
    
    @NotBlank(message = "New email cannot be blank")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
