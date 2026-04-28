package com.pranta.ecommerce.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.AuthResponseDto;
import com.pranta.ecommerce.Dto.UserRequestDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.DuplicateResourceException;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Security.Jwtutil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final Jwtutil jwtutil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserResponseDto register(UserRequestDto dto){

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User with this email already exists");
        }
        
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);
        user.setAddress(dto.getAddress());

        User saveUser = userRepository.save(user);
        
        return mapToDto(saveUser);
    }

    
    public AuthResponseDto login(UserRequestDto request){

        User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid Credentials");
        }

        String token = jwtutil.generateAccessToken(user.getEmail(),user.getRole().name());

        UserResponseDto userDto = mapToDto(user);

        return new AuthResponseDto(token,userDto);
    }
    private UserResponseDto mapToDto(User user){
        UserResponseDto dto = new UserResponseDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setAddress(user.getAddress());

        return dto;

    }
}
