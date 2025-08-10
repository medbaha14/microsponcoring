package com.example.microsponsoringbackend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "Backend is running!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return "Echo: " + message;
    }
} 