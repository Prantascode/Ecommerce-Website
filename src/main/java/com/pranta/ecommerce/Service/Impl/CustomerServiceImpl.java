package com.pranta.ecommerce.Service.Impl;

import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.Request.CustomerRequestDto;
import com.pranta.ecommerce.Dto.Response.CustomerResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.CustomerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponseDto getMyProfileDetails(String email) {
        log.debug("Fetching profile details for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + email));

        return mapToDto(customer);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateMyProfileDetails(String email, CustomerRequestDto dto) {
        log.info("Updating profile details for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + email));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
            log.debug("Updated name to: {}", dto.getName().trim());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            String phone = dto.getPhone().trim();

            if (customerRepository.existsByPhoneAndIdNot(phone, customer.getId())) {
                throw new InvalidRequestException("Phone number already exists: " + phone);
            }

            customer.setPhone(phone);
            log.debug("Updated phone to: {}", phone);
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            customer.setAddress(dto.getAddress().trim());
            log.debug("Updated address to: {}", dto.getAddress().trim());
        }

        if (dto.getCity() != null && !dto.getCity().isBlank()) {
            customer.setCity(dto.getCity().trim());
            log.debug("Updated city to: {}", dto.getCity().trim());
        }

        if (dto.getCountry() != null && !dto.getCountry().isBlank()) {
            customer.setCountry(dto.getCountry().trim());
            log.debug("Updated country to: {}", dto.getCountry().trim());
        }

        if (dto.getPostCode() != null && !dto.getPostCode().isBlank()) {
            customer.setPostCode(dto.getPostCode().trim());
            log.debug("Updated post code to: {}", dto.getPostCode().trim());
        }

        userRepository.save(user);
        Customer updatedCustomer = customerRepository.save(customer);
        
        log.info("Profile updated successfully for user: {}", email);
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