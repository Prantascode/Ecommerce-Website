package com.pranta.ecommerce.Service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pranta.ecommerce.Dto.Request.UpdateEmailDto;
import com.pranta.ecommerce.Dto.Response.UserResponseDto;
import com.pranta.ecommerce.Entity.User;

public interface UserService {
    
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    
    UserResponseDto getUserById(Long id);
    
    UserResponseDto getUserByEmail(String email);
    
    Page<UserResponseDto> getUserByRole(User.Role role,Pageable pageable);
    
    UserResponseDto myProfile(String email);
    
    String updateMyEmail(String currentEmail, UpdateEmailDto dto);
    
    String updatePassword(String email, String newPassword, String currentPassword);
    
    void deactivateUser(Long userId);
    
    void activateUser(Long userId);
    
    Page<UserResponseDto> getActiveAndDeactiveUser(Boolean active,Pageable pageable);
}