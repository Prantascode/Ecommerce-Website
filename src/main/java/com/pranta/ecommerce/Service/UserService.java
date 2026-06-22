package com.pranta.ecommerce.Service;

import java.util.List;

import com.pranta.ecommerce.Dto.Request.UpdateEmailDto;
import com.pranta.ecommerce.Dto.Response.UserResponseDto;
import com.pranta.ecommerce.Entity.User;

public interface UserService {
    
    List<UserResponseDto> getAllUsers();
    
    UserResponseDto getUserById(Long id);
    
    UserResponseDto getUserByEmail(String email);
    
    List<UserResponseDto> getUserByRole(User.Role role);
    
    UserResponseDto myProfile(String email);
    
    String updateMyEmail(String currentEmail, UpdateEmailDto dto);
    
    String updatePassword(String email, String newPassword, String currentPassword);
    
    void deactivateUser(Long userId);
    
    void activateUser(Long userId);
    
    List<UserResponseDto> getActiveAndDeactiveUser(Boolean active);
}