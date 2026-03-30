package com.pranta.ecommerce.Security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pranta.ecommerce.Config.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter{

    private final Jwtutil jwtutil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                
            final String authHeader = request.getHeader("Authorization");
            String email = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);

                try {
                    email = jwtutil.extractEmail(jwt);
                } catch (Exception e) {
                   log.error("jwt error: {}", e.getMessage());
                }
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                if (jwtutil.validateToken(jwt, userDetails)) {
                    log.info("Authentication: {} | Roles: {}", email,userDetails.getAuthorities());
                
                    UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());

                    authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authtoken);
                }
            }
            filterChain.doFilter(request, response);
    }
    
}
