package com.pranta.ecommerce.Service;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.CustomerRequestDto;
import com.pranta.ecommerce.Dto.CustomerResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CustomerResponseDto getMyProfileDetails(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapToDto(customer);
    }

    @Transactional
    public CustomerResponseDto updateMyProfileDetails(String email, CustomerRequestDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            String phone = dto.getPhone().trim();

            if (customerRepository.existsByPhoneAndIdNot(phone, customer.getId())) {
                throw new InvalidRequestException("Phone number already exists");
            }

            customer.setPhone(phone);
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            customer.setAddress(dto.getAddress().trim());
        }

        if (dto.getCity() != null && !dto.getCity().isBlank()) {
            customer.setCity(dto.getCity().trim());
        }

        if (dto.getCountry() != null && !dto.getCountry().isBlank()) {
            customer.setCountry(dto.getCountry().trim());
        }

        if (dto.getPostCode() != null && !dto.getPostCode().isBlank()) {
            customer.setPostCode(dto.getPostCode().trim());
        }

        userRepository.save(user);
        Customer updatedCustomer = customerRepository.save(customer);

        return mapToDto(updatedCustomer);
    }

    private CustomerResponseDto mapToDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getUser().getName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getCountry(),
                customer.getPostCode()
        );
    }
}