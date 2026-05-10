package com.pranta.ecommerce.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.LoginRequest;
import com.pranta.ecommerce.Dto.RegistationDto;
import com.pranta.ecommerce.Dto.Register_LoginResponseDto;
import com.pranta.ecommerce.Entity.Customer;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.CustomerRepository;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Security.Jwtutil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final Jwtutil jwtutil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public Register_LoginResponseDto register(RegistationDto dto){

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);

        User saveUser = userRepository.save(user);

        Customer customer = new Customer();

        customer.setName(dto.getName());
        customer.setUser(saveUser);

       Customer saveCustomer = customerRepository.save(customer);
        
        return mapToDto(saveUser, saveCustomer);
    }

    
    public AuthResponseDto login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid Credentials");
        }

        String token = jwtutil.generateAccessToken(user.getEmail(),user.getRole().name());
        Register_LoginResponseDto userDto;
         if (user.getRole() == User.Role.ADMIN) {
            userDto = mapAdminToDto(user);
        } else {
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            userDto = mapToDto(user, customer);
        }
        
        return new AuthResponseDto(token,userDto);
    }

    private Register_LoginResponseDto mapToDto(User user, Customer customer){

        Register_LoginResponseDto dto = new Register_LoginResponseDto();

        dto.setId(user.getId());
        dto.setName(customer.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());

        return dto;

    }

    private Register_LoginResponseDto mapAdminToDto(User user){

        Register_LoginResponseDto dto = new Register_LoginResponseDto();

        dto.setId(user.getId());
        dto.setName("Admin");
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());

        return dto;
    }

}
