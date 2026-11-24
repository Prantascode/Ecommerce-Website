package com.pranta.ecommerce.Controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranta.ecommerce.Dto.Request.PasswordUpdateDto;
import com.pranta.ecommerce.Dto.Request.UpdateEmailDto;
import com.pranta.ecommerce.Dto.Response.PageResponseDto;
import com.pranta.ecommerce.Dto.Response.UserResponseDto;
import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Service.UserService;
import com.pranta.ecommerce.Util.PaginationHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Users",
    description = "Users Management API's"
)
public class UserController {
    
    private final UserService userService;

    private final PaginationHelper paginationHelper;

    @Operation(
        summary = "Get users",
        description = "Get all users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);

        Page<UserResponseDto> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(PageResponseDto.from(users));
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
        description = "Return user info using email"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
        summary = "Get users by role",
        description = "Get all users using role"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role/{role}")
    public ResponseEntity<PageResponseDto<UserResponseDto>> getUserByUserRole(@PathVariable User.Role role,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ){
        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);
        Page<UserResponseDto> users = userService.getUserByRole(role, pageable);

        return ResponseEntity.ok(PageResponseDto.from(users));
    }

    @Operation(
        summary = "Get my profile",
        description = "Get user's own profile"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping({"/me", "/my_profile"})
    public ResponseEntity<UserResponseDto> myProfile(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(userService.myProfile(email));
    }

    @Operation(
        summary = "Update Email",
        description = "Update user's email"
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/me/email")
    public ResponseEntity<String> updateEmail(Authentication authentication,
        @Valid @RequestBody UpdateEmailDto dto){
            String email = authentication.getName();
            return ResponseEntity.ok(userService.updateMyEmail(email, dto));
    }

    @Operation(
        summary = "Update Password",
        description = "Update user's password"
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/me/password")
    public ResponseEntity<String> updatePassword(Authentication authentication,
        @Valid @RequestBody PasswordUpdateDto dto){
            String email = authentication.getName();
            return ResponseEntity.ok(userService.updatePassword(email, dto.getNewPassword(), dto.getOldPassword()));
    }

    @Operation(
        summary = "Deactivate users",
        description = "Deactivate user by ID"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @Operation(
        summary = "Activate users",
        description = "Activate user by ID"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok("User activated successfully");
    }

    @Operation(
        summary = "Get active and inactive users",
        description = "Get all active or inactive users"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active/{active}")
    public ResponseEntity<PageResponseDto<UserResponseDto>> getActiveAndInactiveUsers(@PathVariable Boolean active,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ){
        Pageable pageable = paginationHelper.buildPageable(page, size, sortBy, sortDir);
        Page<UserResponseDto> users = userService.getActiveAndDeactiveUser(active, pageable);

        return ResponseEntity.ok(PageResponseDto.from(users));
    }
}
