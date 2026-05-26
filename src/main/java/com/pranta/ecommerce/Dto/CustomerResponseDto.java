package com.pranta.ecommerce.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDto {

    private Long id;
    
    private String name;

    private String phone;

    private String address;

    private String city;

    private String country;

    private String postCode;
}
