package com.pranta.ecommerce.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pranta.ecommerce.Security.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(Session -> Session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth 
                // Swagger & OpenAPI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // Public Authentication
                .requestMatchers("/api/auth/**").permitAll() 

                // Public Browsing APIs (Storefront)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/brands/**").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/reviews/allReviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/discounts", "/api/discounts/product/**").permitAll()

                // Payment Gateway IPN Callbacks (Public)
                .requestMatchers(
                    "/api/payments/success",
                    "/api/payments/fail",
                    "/api/payments/cancel"
                ).permitAll()

                // Customer / User Authenticated APIs
                .requestMatchers("/api/carts/**").hasRole("USER")
                .requestMatchers("/api/wishlists/**").hasRole("USER")
                .requestMatchers("/api/customers/**").hasRole("USER")
                .requestMatchers("/api/reviews/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/payments/initiate/**").hasRole("USER")

                // Order APIs - USER
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/api/orders/myOrder").hasRole("USER")

                // User self-management endpoints (USER & ADMIN)
                .requestMatchers(
                    "/api/users/me/**",
                    "/api/users/my_profile"
                ).hasAnyRole("USER", "ADMIN")

                // Admin order management
                .requestMatchers(HttpMethod.GET, "/api/orders/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/{orderId}/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/orders/filter/{status}").hasRole("ADMIN")

                // Order APIs - USER
                .requestMatchers(HttpMethod.GET, "/api/orders/{orderId}").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/orders/{orderId}/cancel").hasRole("USER")

                // Admin product management
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                // Admin category management
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                // Admin brand management
                .requestMatchers(HttpMethod.POST, "/api/brands/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/brands/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/brands/**").hasRole("ADMIN")

                // Admin General & User/Discount Management
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/discounts/**").hasRole("ADMIN")

                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
