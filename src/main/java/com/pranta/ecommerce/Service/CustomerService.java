package com.pranta.ecommerce.Service;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CustomerResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomerService {
    
    private final UserRepository userRepository;

    private final CustomerRepository customerRepository;

    public CustomerResponseDto getMyProfileDetails(String email){

        User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email "+email));
        
        Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            
        return mapToDto(customer);
    }

    private CustomerResponseDto mapToDto(Customer customer){
       return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getCountry(),
                customer.getPostCode()
       );
    }
}
