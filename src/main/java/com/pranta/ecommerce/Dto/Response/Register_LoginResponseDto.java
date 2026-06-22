package com.pranta.ecommerce.Dto.Response;

import com.pranta.ecommerce.Entity.User.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Register_LoginResponseDto {
    private Long id;
    private String name;
    private String email; 
    private Role role;
    private boolean active;
}
