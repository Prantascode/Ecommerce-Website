package com.pranta.ecommerce.Service;

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

    public UserResponseDto createUser(UserRequestDto dto){

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(User.Role.USER);

        User saveUser = userRepository.save(user);
        
        return new UserResponseDto(
            saveUser.getId(),
            saveUser.getName(),
            saveUser.getEmail()
        );
    }
}
