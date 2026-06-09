package com.pranta.ecommerce.Dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.pranta.ecommerce.Entity.User.Role;

import lombok.Data;


@Data
@JsonPropertyOrder({"id", "name", "email", "role", "active"})
public class AdminResponseDto implements UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
}
