package com.pranta.ecommerce.Dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.pranta.ecommerce.Entity.User;

import lombok.Data;

@Data
@JsonPropertyOrder({"id", "customerId", "name", "email", "role", "active", "phone", "address", "city", "country", "postCode"})
public class CostomCustomerResponseDto implements UserResponseDto{
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private boolean active;

    private Long customerId;
    private String address;
    private String phone;
    private String city;
    private String country;
    private String postCode;
}
