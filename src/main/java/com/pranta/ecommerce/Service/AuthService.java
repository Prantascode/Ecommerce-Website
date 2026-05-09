package com.pranta.ecommerce.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.UserRequestDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
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

    public UserResponseDto register(UserRequestDto dto){

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        
        User user = new User();
        //user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);
        //user.setAddress(dto.getAddress());

        User saveUser = userRepository.save(user);

        Customer customer = new Customer();

        customer.setName(dto.getName());
        customer.setUser(saveUser);

       Customer saveCustomer = customerRepository.save(customer);
        
        return mapToDto(saveUser, saveCustomer);
    }

    
    public AuthResponseDto login(UserRequestDto request){

        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid Credentials");
        }

        Customer customer = customerRepository.findByUser(user)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String token = jwtutil.generateAccessToken(user.getEmail(),user.getRole().name());

        UserResponseDto userDto = mapToDto(user, customer);

        return new AuthResponseDto(token,userDto);
    }
    private UserResponseDto mapToDto(User user, Customer customer){
        UserResponseDto dto = new UserResponseDto();

        dto.setId(user.getId());
        dto.setCustomerId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;

    }
}
