package com.example.microsponsoringbackend.config;

import com.example.microsponsoringbackend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to upload profile pictures
                .requestMatchers(HttpMethod.POST, "/api/upload/profile-picture").permitAll()
                // Allow organisation image uploads
                .requestMatchers(HttpMethod.POST, "/api/upload/organisation-logo").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.POST, "/api/upload/organisation-banner").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.POST, "/api/upload/organisation-background").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                // Allow anyone to access images
                .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                // Allow all user endpoints
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/companies-non-profits").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/companies-non-profits/{id}").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.GET, "/api/companies-non-profits/user/{userId}").permitAll()
                // Allow authenticated users to get or update their own user by id
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/{id}/organisation-profile").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}/organisation-profile").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                // Recognition Benefits - Allow organisations to manage their benefits
                .requestMatchers(HttpMethod.GET, "/api/recognition-benefits").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.GET, "/api/recognition-benefits/{id}").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.POST, "/api/recognition-benefits").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.PUT, "/api/recognition-benefits/**").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.DELETE, "/api/recognition-benefits/**").hasAnyRole("ADMIN", "ORGANISATION_NONPROFIT")
                .requestMatchers(HttpMethod.GET, "/api/recognition-benefits/company/**").permitAll()
                // Bank Accounts - Allow authenticated users to manage their own bank accounts
                .requestMatchers(HttpMethod.GET, "/api/bank-accounts/user/{userId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/bank-accounts/{id}").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/bank-accounts").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/bank-accounts/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/bank-accounts/{id}").authenticated()
                // Payment Processing - Allow authenticated users to process payments and view invoices
                .requestMatchers(HttpMethod.POST, "/api/payments/process").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/payments/checkout-session").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/sponsor/{sponsorId}/invoices").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/company/{companyId}/invoices").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/sponsor/{sponsorId}/stats").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/company/{companyId}/stats").authenticated()
                // Allow public access to invoice PDFs
                .requestMatchers(HttpMethod.GET, "/api/invoices/*/pdf").permitAll()
                // Auth endpoints and public endpoints
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/role/{role}").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}