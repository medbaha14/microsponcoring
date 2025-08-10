package com.example.microsponsoringbackend.service;

import com.example.microsponsoringbackend.model.PasswordResetToken;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${password.reset.token.expiration:3600000}")
    private long tokenExpirationMs;
    
    public boolean initiatePasswordReset(String email) {
        logger.info("[RESET] Recherche utilisateur pour email: {}", email);
        
        User user = null;
        
        // Test direct de la base de données
        try {
            List<User> allUsers = userService.findAll();
            logger.info("[RESET] Nombre total d'utilisateurs en base: {}", allUsers.size());
            for (User u : allUsers) {
                logger.info("[RESET] User: id={}, email={}, username={}", u.getUserId(), u.getEmail(), u.getUsername());
            }
            
            // Recherche directe par email
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.warn("[RESET] Aucun utilisateur trouvé pour email: {}", email);
                return false;
            }
            
            user = userOpt.get();
            logger.info("[RESET] Utilisateur trouvé directement: id={}, email={}, username={}", 
                user.getUserId(), user.getEmail(), user.getUsername());
            
            // Vérification en base avec cet ID
            Optional<User> verifiedUser = userService.findById(user.getUserId());
            if (verifiedUser.isPresent()) {
                logger.info("[RESET] Vérification base OK: utilisateur {} trouvé en base", verifiedUser.get().getEmail());
            } else {
                logger.error("[RESET] ERREUR: L'utilisateur avec ID {} n'existe pas en base!", user.getUserId());
                return false;
            }
        } catch (Exception e) {
            logger.error("[RESET] Erreur lors de la récupération de tous les utilisateurs: {}", e.getMessage());
            return false;
        }
        
        if (user == null) {
            logger.error("[RESET] Utilisateur null après récupération");
            return false;
        }
        
        try {
            // Delete any existing tokens for this user
            tokenRepository.deleteByUserId(user.getUserId().toString());
        } catch (Exception e) {
            // Log the error but continue with the process
            System.err.println("Error deleting existing tokens: " + e.getMessage());
        }
        
        // Create new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(tokenExpirationMs / 1000);
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenId(UUID.randomUUID().toString()); // Manually generate tokenId
        resetToken.setToken(token);
        logger.info("[RESET] Création du token pour user_id: {}", user.getUserId());
        resetToken.setUserId(user.getUserId().toString()); // Convert UUID to string
        resetToken.setExpiryDate(expiryDate);
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        // Send email
        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        logger.info("Appel à sendPasswordResetEmail pour {}", user.getEmail());
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink, user.getUsername());
        
        return true;
    }
    
    public boolean resetPassword(String token, String newPassword) {
        logger.info("[RESET] Tentative de reset avec token: {}", token);
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            logger.warn("[RESET] Token non trouvé ou déjà utilisé: {}", token);
            return false;
        }
        PasswordResetToken resetToken = tokenOpt.get();
        logger.info("[RESET] Token trouvé pour user_id: {}, expiry_date: {}, now: {}", resetToken.getUserId(), resetToken.getExpiryDate(), LocalDateTime.now());
        if (resetToken.isExpired()) {
            logger.warn("[RESET] Token expiré: {}", token);
            return false;
        }
        
        // Update password
        User user = userService.findById(UUID.fromString(resetToken.getUserId())).orElse(null);
        if (user == null) {
            logger.warn("[RESET] Utilisateur non trouvé pour user_id: {}", resetToken.getUserId());
            return false;
        }
        logger.info("[RESET] Utilisateur trouvé: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new java.util.Date());
        userService.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return true;
    }
    
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            logger.warn("Token non trouvé ou déjà utilisé: {}", token);
            return false;
        }
        PasswordResetToken resetToken = tokenOpt.get();
        logger.info("Token trouvé, expiry_date: {}, now: {}", resetToken.getExpiryDate(), LocalDateTime.now());
        if (resetToken.isExpired()) {
            logger.warn("Token expiré: {}", token);
            return false;
        }
        return true;
    }
} 