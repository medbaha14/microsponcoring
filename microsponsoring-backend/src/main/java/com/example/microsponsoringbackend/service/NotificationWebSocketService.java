package com.example.microsponsoringbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.microsponsoringbackend.model.Notification;

@Service
public class NotificationWebSocketService {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * Send a notification to a specific user in real-time
	 */
	public void sendNotificationToUser(String username, Notification notification) {
		messagingTemplate.convertAndSendToUser(username, // must match Principal name in Spring Security
				"/queue/notifications", notification);
	}
}
