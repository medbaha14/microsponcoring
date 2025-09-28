package com.example.microsponsoringbackend.config;

import com.example.microsponsoringbackend.security.JwtAuthenticationFilter;
import com.example.microsponsoringbackend.service.DatabaseDrivenSecurityService;
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
    
    @Autowired
    private DatabaseDrivenSecurityService databaseDrivenSecurityService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

    .requestMatchers("/actuator/**", "/api/health/**", "/api/auth/**", "/api/public/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/upload/profile-picture").permitAll()

    // --- RECOGNITION BENEFITS (ordre important) ---
    .requestMatchers(HttpMethod.GET, "/api/recognition-benefits/company/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/recognition-benefits").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.GET, "/api/recognition-benefits/{id}").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.POST, "/api/recognition-benefits").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.PUT, "/api/recognition-benefits/**").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.DELETE, "/api/recognition-benefits/**").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")

    // --- USERS ---
    .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
    .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
    .requestMatchers(HttpMethod.PATCH, "/api/users/{id}").authenticated()
    .requestMatchers(HttpMethod.POST, "/api/users/{id}/initialize-profiles").authenticated()
    .requestMatchers(HttpMethod.GET, "/api/users/{id}/organisation-profile").permitAll()
    .requestMatchers(HttpMethod.PUT, "/api/users/{id}/organisation-profile").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.GET, "/api/users/role/{role}").permitAll()

    // --- SPONSORS ---
    .requestMatchers(HttpMethod.GET, "/api/sponsors").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/sponsors/{id}").hasAnyRole("ADMIN","SPONSOR")
    .requestMatchers(HttpMethod.GET, "/api/sponsors/user/*").authenticated()
    .requestMatchers(HttpMethod.POST, "/api/sponsors").hasAnyRole("ADMIN","SPONSOR")
    .requestMatchers(HttpMethod.PUT, "/api/sponsors/{id}").hasAnyRole("ADMIN","SPONSOR")
    .requestMatchers(HttpMethod.DELETE, "/api/sponsors/{id}").hasRole("ADMIN")

    // --- COMPANIES ---
    .requestMatchers(HttpMethod.GET, "/api/companies-non-profits").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/companies-non-profits/{id}").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.GET, "/api/companies-non-profits/user/{userId}").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/companies-non-profits").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.PUT, "/api/companies-non-profits/{id}").hasAnyRole("ADMIN","ORGANISATION_NONPROFIT")
    .requestMatchers(HttpMethod.DELETE, "/api/companies-non-profits/{id}").hasRole("ADMIN")

    .requestMatchers("/api/payment-transactions/**").hasAnyRole("ADMIN","SPONSOR")
    .requestMatchers("/api/sample-data/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET, "/api/invoices/*/pdf").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/invoices/**").authenticated()
    // Permit SockJS endpoints
    .requestMatchers("/ws-notifications/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/notifications/**").authenticated()

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