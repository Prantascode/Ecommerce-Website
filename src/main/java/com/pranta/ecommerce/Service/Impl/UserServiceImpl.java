package com.pranta.ecommerce.Service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.AdminResponseDto;
import com.pranta.ecommerce.Dto.CostomCustomerResponseDto;
import com.pranta.ecommerce.Dto.UpdateEmailDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
     
    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(user -> {
                    Customer customer = null;
                    if (user.getRole() != User.Role.ADMIN) {
                        customer = customerRepository.findByUser(user).orElse(null);
                    }
                    return mapToUserResponseDto(user, customer);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));
        
        // Only fetch customer for non-admin users
        Customer customer = null;
        if (user.getRole() != User.Role.ADMIN) {
            customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this user"));
        }
        
        return mapToUserResponseDto(user, customer);
    }

    @Override
    public UserResponseDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Email"));

        // Only fetch customer for non-admin users
        Customer customer = null;
        if (user.getRole() != User.Role.ADMIN) {
            customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this user"));
        }

        return mapToUserResponseDto(user, customer);
    }

    @Override
    public List<UserResponseDto> getUserByRole(User.Role role){
        List<User> users = userRepository.findByRole(role);
        
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        
        return users.stream()
                .map(user -> {
                    // If role is ADMIN, don't fetch customer
                    Customer customer = null;
                    if (role != User.Role.ADMIN) {
                        customer = customerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for user: " + user.getEmail()));
                    }
                    return mapToUserResponseDto(user, customer);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto myProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        // Only fetch customer for non-admin users (optional, don't throw exception)
        Customer customer = null;
        if (user.getRole() != User.Role.ADMIN) {
            customer = customerRepository.findByUser(user).orElse(null);
        }
        
        return mapToUserResponseDto(user, customer);
    }

    @Override
    @Transactional
    public String updateMyEmail(String currentEmail, UpdateEmailDto dto) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new InvalidRequestException("Invalid password");
        }

        if(userRepository.existsByEmail(dto.getNewEmail())){
            throw new InvalidRequestException("Email already exists");
        }
        user.setEmail(dto.getNewEmail().trim());

       return "Email updated successfully, Please login again with your new email";
    }

    @Override
    @Transactional
    public String updatePassword(String email, String newPassword, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new InvalidRequestException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

       return "Password updated successfully";
    }

    @Override
    public void deactivateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with this id"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentAdmin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN not found"));

        if (currentAdmin.getId().equals(userId)) {
            throw new InvalidRequestException("Admin can't deactivate their own account");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with this id"));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public List<UserResponseDto> getActiveAndDeactiveUser(Boolean active){
        List<User> users = userRepository.findByActive(active);

        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        
        return users.stream()
                .map(user -> {
                    // Only fetch customer for non-admin users
                    Customer customer = null;
                    if (user.getRole() != User.Role.ADMIN) {
                        Optional<Customer> optionalCustomer = customerRepository.findByUser(user);
                        customer = optionalCustomer.orElse(null);
                    }
                    return mapToUserResponseDto(user, customer);
                })
                .collect(Collectors.toList());         
    }
    
    // Single mapping method - always returns UserResponseDto
    private UserResponseDto mapToUserResponseDto(User user, Customer customer){
        if (user.getRole() == User.Role.ADMIN) {
            AdminResponseDto dto = new AdminResponseDto();

            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());
            dto.setRole(user.getRole());
            dto.setActive(user.isActive());

            return dto;
        }
        CostomCustomerResponseDto dto = new CostomCustomerResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        
        // Only set customer details if customer exists AND user is not admin
        if (customer != null && user.getRole() != User.Role.ADMIN) {
            dto.setCustomerId(customer.getId());
            dto.setAddress(customer.getAddress());
            dto.setPhone(customer.getPhone());
            dto.setCity(customer.getCity());
            dto.setCountry(customer.getCountry());
            dto.setPostCode(customer.getPostCode());
        }
        return dto;
    }
}