package com.pranta.ecommerce.Service;

import com.pranta.ecommerce.Dto.CustomerRequestDto;
import com.pranta.ecommerce.Dto.CustomerResponseDto;

public interface CustomerService {
    
    CustomerResponseDto getMyProfileDetails(String email);
    
    CustomerResponseDto updateMyProfileDetails(String email, CustomerRequestDto dto);
}