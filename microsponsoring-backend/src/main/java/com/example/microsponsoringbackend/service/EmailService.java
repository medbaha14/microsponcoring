package com.example.microsponsoringbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    public void sendPasswordResetEmail(String to, String resetLink, String username) {
        logger.info("Tentative d'envoi d'email de réinitialisation à {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request - Microsponsoring");
        message.setText(createPasswordResetEmailContent(resetLink, username));
        message.setFrom("bousnina.baha14@gmail.com");
        try {
            mailSender.send(message);
            logger.info("Email de réinitialisation envoyé avec succès à {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de réinitialisation à " + to, e);
            throw e;
        }
    }
    
    private String createPasswordResetEmailContent(String resetLink, String username) {
        return String.format(
            "Hello %s,\n\n" +
            "You have requested to reset your password for your Microsponsoring account.\n\n" +
            "Please click on the following link to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 1 hour for security reasons.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Microsponsoring Team",
            username, resetLink
        );
    }
} 