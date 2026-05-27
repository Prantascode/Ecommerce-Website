package com.pranta.ecommerce.Service;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.UpdateEmailDto;
import com.pranta.ecommerce.Dto.UserRequestDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
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
public class UserService {

    private final UserRepository userRepository;

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;
     
    public List<UserResponseDto> getAllUsers() {
        
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users
                .stream()
                .map(user -> {
                    Customer customer = customerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    return mapResponseDto(user, customer);
                })
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapResponseDto(user,customer);
    }

    public UserResponseDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Email"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapResponseDto(user,customer);
    }

    public List<UserResponseDto> getUserByRole(User.Role role){
        List<User> users = userRepository.findByRole(role);
        
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(user -> {
                    Customer customer = customerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    return mapResponseDto(user, customer);
                })
                .toList();
    }

    public UserResponseDto myProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return mapResponseDto(user,customer);
    }

    @Transactional
    public String UpdateMyEmail(String currentEmail, UpdateEmailDto dto) {
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

    @Transactional
    public String UpdatePassword(String email, String newPassword, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            throw new InvalidRequestException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

       return "Password updated successfully";
    }


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

    public void activateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with this id"));
        user.setActive(true);
        userRepository.save(user);
    }

    public List<UserResponseDto> getActiveAndDeactiveUser(Boolean active){
        List<User> users = userRepository.findByActive(active);

        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(user -> {
                    Customer customer = customerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    return mapResponseDto(user, customer);
                })
                .toList();
               
    }
    private UserResponseDto mapResponseDto(User user,Customer customer){
        return new UserResponseDto(
                user.getId(),
                customer.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                customer.getAddress(),
                customer.getPhone(),
                customer.getCity(),
                customer.getCountry(),
                customer.getPostCode()
        );

    }
}

