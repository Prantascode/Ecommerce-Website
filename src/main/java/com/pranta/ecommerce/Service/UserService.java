package com.pranta.ecommerce.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.UserRequestDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
     
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()))
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

