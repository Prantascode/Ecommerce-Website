package com.pranta.ecommerce.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.UserRepository;
import com.pranta.ecommerce.Security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found" + email));
        
    if (!user.isActive()) {
        throw new RuntimeException("User account is deactivated");
    }

        return new CustomUserDetails(user);
    }
    
}
