package com.pranta.ecommerce.Dto;

import com.pranta.ecommerce.Entity.User.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email; 
    private Role role;
}
