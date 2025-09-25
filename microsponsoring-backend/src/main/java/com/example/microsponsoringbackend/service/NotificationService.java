package com.example.microsponsoringbackend.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.microsponsoringbackend.model.Notification;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	public Notification create(User user, String message) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setIsRead(false);
		return notificationRepository.save(notification);
	}

	public void markAsRead(UUID notificationId) {
		Notification notif = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new IllegalArgumentException("Notification not found"));
		notif.setIsRead(true);
		notificationRepository.save(notif);
	}
}
