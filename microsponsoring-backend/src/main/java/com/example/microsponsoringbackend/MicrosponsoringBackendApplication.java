package com.example.microsponsoringbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MicrosponsoringBackendApplication {
    public static void main(String[] args) {
        // Load .env variables
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        SpringApplication.run(MicrosponsoringBackendApplication.class, args);
    }
} 