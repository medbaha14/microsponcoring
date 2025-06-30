package com.example.microsponsoringbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckoutConfig {

    @Value("${checkout.secret.key}")
    private String secretKey;

    @Value("${checkout.public.key}")
    private String publicKey;

    @Bean
    public String checkoutSecretKey() {
        return secretKey;
    }

    @Bean
    public String checkoutPublicKey() {
        return publicKey;
    }
} 