package com.example.microsponsoringbackend.security;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("=== CustomUserDetailsService.loadUserByUsername called for username: {} ===", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        logger.info("User found in database:");
        logger.info("  - Username: {}", user.getUsername());
        logger.info("  - UserType: {}", user.getUserType());
        logger.info("  - UserType.name(): {}", user.getUserType().name());
        logger.info("  - Active: {}", user.getActive());

        // Map your UserType to a role string with ROLE_ prefix for Spring Security
        String role = "ROLE_" + user.getUserType().name();
        logger.info("Generated role: {}", role);

        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        logger.info("Created GrantedAuthority: {}", authority.getAuthority());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
        
        logger.info("Final UserDetails authorities: {}", userDetails.getAuthorities());
        logger.info("=== CustomUserDetailsService.loadUserByUsername completed ===");
        
        return userDetails;
    }
} 