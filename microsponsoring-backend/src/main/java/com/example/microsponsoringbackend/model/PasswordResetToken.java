package com.example.microsponsoringbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {
    
    @Id
    @Column(name = "token_id", columnDefinition = "VARCHAR(36)")
    private String tokenId;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private boolean used = false;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
} 