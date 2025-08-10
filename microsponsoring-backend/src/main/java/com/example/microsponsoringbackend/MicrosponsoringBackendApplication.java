package com.example.microsponsoringbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MicrosponsoringBackendApplication {
    public static void main(String[] args) {
        try {
            // Load .env variables with error handling
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
            
            // Set environment variables for Spring Boot to use
            String checkoutSecretKey = dotenv.get("CHECKOUT_SECRET_KEY");
            String checkoutPublicKey = dotenv.get("CHECKOUT_PUBLIC_KEY");
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");
            String serverPort = dotenv.get("SERVER_PORT");
            
            // Set environment variables if values are not null
            if (checkoutSecretKey != null) {
                System.setProperty("CHECKOUT_SECRET_KEY", checkoutSecretKey);
            }
            if (checkoutPublicKey != null) {
                System.setProperty("CHECKOUT_PUBLIC_KEY", checkoutPublicKey);
            }
            if (dbUrl != null) {
                System.setProperty("DB_URL", dbUrl);
            }
            if (dbUsername != null) {
                System.setProperty("DB_USERNAME", dbUsername);
            }
            if (dbPassword != null) {
                System.setProperty("DB_PASSWORD", dbPassword);
            }
            if (serverPort != null) {
                System.setProperty("SERVER_PORT", serverPort);
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
            System.err.println("Using default configuration values.");
        }
        
        SpringApplication.run(MicrosponsoringBackendApplication.class, args);
    }
} 