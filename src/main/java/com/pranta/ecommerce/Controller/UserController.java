package com.pranta.ecommerce.Controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.Request.PasswordUpdateDto;
import com.pranta.ecommerce.Dto.Request.UpdateEmailDto;
import com.pranta.ecommerce.Dto.Response.UserResponseDto;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@Tag(
    name = "Users",
    description = "Users Management API's"
)
public class UserController {
    
    private final UserService userService;

    @Operation(
        summary = "Get users",
        description = "Get all users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
        summary = "Get users by Id",
        description = "Return user info using Id"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
        summary = "Get user by email",
        description = "Get user by Id"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmailId(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
        summary = "Get users by role",
        description = "Get all users using role"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDto>> getUserByUserRole(@PathVariable User.Role role){
        return ResponseEntity.ok(userService.getUserByRole(role));
    }

    @Operation(
        summary = "Get my_profile",
        description = "Get users own profile"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/my_profile")
    public ResponseEntity<UserResponseDto> myProfile(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(userService.myProfile(email));
    }

    @Operation(
        summary = "Update Email",
        description = "update users email"
    )
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/me/email")
    public ResponseEntity<String> updateEmail(Authentication authentication,
        @RequestBody UpdateEmailDto dto){
            String email = authentication.getName();
            return ResponseEntity.ok(userService.updateMyEmail(email, dto));
    }
    @Operation(
        summary = "Update Password",
        description = "update users Password"
    )
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/me/password")
    public ResponseEntity<String> updatePassword(Authentication authentication,
        @RequestBody PasswordUpdateDto dto){
            String email = authentication.getName();
            return ResponseEntity.ok(userService.updatePassword(email, dto.getNewPassword(), dto.getOldPassword()));
    }

    @Operation(
        summary = "Deactivate users",
        description = "Deactivate users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
       return ResponseEntity.ok("User Deactivated");
    }

    @Operation(
        summary = "Activate users",
        description = "Activate users"
    )
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User activated successfully");
    }

    @Operation(
        summary = "Get Active and Deactive users",
        description = "Get All Active and Deactive users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active/{active}")
    public ResponseEntity<List<UserResponseDto>> getActiveAndDeactiveUsers(@PathVariable Boolean active){
        
        return ResponseEntity.ok(userService.getActiveAndDeactiveUser(active));
    }
}
