package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.Request.CustomerRequestDto;
import com.pranta.ecommerce.Dto.Response.CustomerResponseDto;

public interface CustomerService {
    
    CustomerResponseDto getMyProfileDetails(String email);
    
    CustomerResponseDto updateMyProfileDetails(String email, CustomerRequestDto dto);
}