package com.pranta.ecommerce.Service;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Dto.UserRequestDto;
import com.pranta.ecommerce.Dto.UserResponseDto;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Exceptions.InvalidRequestException;
import com.pranta.ecommerce.Exceptions.ResourceNotFoundException;
import com.pranta.ecommerce.Repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
     
    public List<UserResponseDto> getAllUsers() {
        
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users
                .stream()
                .map(this::mapResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this id"));

        return mapResponseDto(user);
    }

    public UserResponseDto getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this Email"));

        return mapResponseDto(user);
    }

    public List<UserResponseDto> getUserByRole(User.Role role){
        List<User> users = userRepository.findByRole(role);
        
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::mapResponseDto)
                .toList();
    }

    public UserResponseDto myProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));

        return mapResponseDto(user);
    }

    @Transactional
    public UserResponseDto UpdateMyProfile(String email,UserRequestDto dto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by this mail"));
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(dto.getPassword());
        user.setAddress(dto.getAddress());

        User updated = userRepository.save(user);

        return mapResponseDto(updated);
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
                .map(this::mapResponseDto)
                .toList();
               
    }
    private UserResponseDto mapResponseDto(User user){
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getAddress()
        );

    }
}

