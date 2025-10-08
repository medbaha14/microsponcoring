package com.example.microsponsoringbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.microsponsoringbackend.model.Notification;
import com.example.microsponsoringbackend.service.NotificationService;
import com.example.microsponsoringbackend.enums.NotificationType;

@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationService notificationService;
    
    @MessageMapping("/notification.send")
    @SendTo("/topic/notifications")
    public Notification sendNotification(@Payload Notification notification, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Received notification: {}", notification);
        
        // Get the authenticated user
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null) {
            logger.info("Notification from user: {}", auth.getName());
        }
        
        // Save notification to database
        try {
            // Use the create method from NotificationService
            Notification savedNotification = notificationService.create(
                notification.getUser(), 
                notification.getMessage(), 
                notification.getType()
            );
            logger.info("Notification saved with ID: {}", savedNotification.getNotificationId());
            return savedNotification;
        } catch (Exception e) {
            logger.error("Error saving notification: {}", e.getMessage());
            return notification;
        }
    }
    
    @MessageMapping("/notification.subscribe")
    public void subscribeToNotifications(SimpMessageHeaderAccessor headerAccessor) {
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null) {
            logger.info("User {} subscribed to notifications", auth.getName());
            
            // Send a welcome message
            Notification welcomeNotification = new Notification();
            welcomeNotification.setMessage("You are now connected to the notification service");
            welcomeNotification.setType(NotificationType.INCOMING);
            
            messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/notifications", welcomeNotification);
        }
    }
    
    @MessageMapping("/notification.ping")
    @SendTo("/topic/pong")
    public String handlePing(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Received ping: {}", message);
        return "Pong: " + message;
    }
}
