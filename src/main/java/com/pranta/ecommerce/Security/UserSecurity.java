package com.pranta.ecommerce.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Repository.UserRepository;

@Component("userSecurity")
public class UserSecurity {
    
    @Autowired
    private UserRepository userRepository;

    public boolean isCurrentUserId(Long userId, String email){
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }
}
