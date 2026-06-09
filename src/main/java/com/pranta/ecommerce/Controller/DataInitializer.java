package com.pranta.ecommerce.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Entity.User;
import com.pranta.ecommerce.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        // Check if admin exists using the configured email
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin already exists with email: {}", adminEmail);
            return;
        }

        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        log.info("========================================");
        log.info("✅ Admin Created Successfully!");
        log.info("📧 Email: {}", adminEmail);
        log.info("========================================");
        
        // Only show password in development
        if (isDevelopment()) {
            log.info("⚠️  Password: {}", adminPassword);
            log.info("⚠️  PLEASE CHANGE THIS PASSWORD ON FIRST LOGIN!");
        }
    }
    
    private boolean isDevelopment() {
        // You can check active profiles
        return true; // or implement proper profile check
    }
}