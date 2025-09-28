package com.example.microsponsoringbackend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.microsponsoringbackend.model.Notification;
import com.example.microsponsoringbackend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	// Get notifications for current user (from JWT)
	@GetMapping("/my-notifications")
	public ResponseEntity<Page<Notification>> getMyNotifications(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "false") boolean unread) {
	    try {
	        Page<Notification> notifications = notificationService.getNotificationsForCurrentUser(page, size, unread);
	        return ResponseEntity.ok(notifications);
	    } catch (Exception e) {
	        return ResponseEntity.status(500).build();
	    }
	}

	// Get unread count for current user
	@GetMapping("/my-unread-count")
	public ResponseEntity<Long> getMyUnreadCount() {
		try {
			// You'll need to implement this method in NotificationService
			Long count = notificationService.getUnreadCountForCurrentUser();
			return ResponseEntity.ok(count);
		} catch (Exception e) {
			return ResponseEntity.status(500).build();
		}
	}

	// Mark notification as read
	@PutMapping("/{notificationId}/read")
	public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
		try {
			notificationService.markAsRead(notificationId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(500).build();
		}
	}

	// Mark all as read for current user
	@PutMapping("/mark-all-read")
	public ResponseEntity<Void> markAllAsRead() {
		try {
			notificationService.markAllAsReadForCurrentUser();
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(500).build();
		}
	}

	// Delete notification
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
		try {
			notificationService.deleteNotification(notificationId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(500).build();
		}
	}

	// Create a new notification (mock / test data)
	@PostMapping("/mock")
	public ResponseEntity<Notification> createMockNotification() {
	    Notification saved = notificationService.createMock();
	    return ResponseEntity.ok(saved);
	}

}