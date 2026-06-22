package com.pranta.ecommerce.Dto.Request;



import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserRequestDto {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is Required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is Required")
    @Column(nullable = false)
    private String password;

    @Pattern(
            regexp = "^(\\+8801|01)[3-9][0-9]{8}$",
            message = "Invalid Bangladeshi phone number"
    )
    @Column(unique = true)
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    private String postCode;
}
