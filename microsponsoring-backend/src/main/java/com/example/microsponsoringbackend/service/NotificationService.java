package com.example.microsponsoringbackend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.microsponsoringbackend.enums.NotificationType;
import com.example.microsponsoringbackend.model.Notification;
import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationWebSocketService webSocketService;

    @Autowired
    private UserService userService;

	public Notification create(User user, String message, NotificationType type) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setIsRead(false);
		notification.setType(type);

		// Send real-time notification via WebSocket
		webSocketService.sendNotificationToUser(user.getUsername(), notification);

		return notificationRepository.save(notification);
	}

	public void markAsRead(UUID notificationId) {
		Notification notif = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new IllegalArgumentException("Notification not found"));
		notif.setIsRead(true);
		notificationRepository.save(notif);
	}

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // Fetch all notifications for current user
    public Page<Notification> getNotificationsForCurrentUser(int page, int size, boolean unread) {
        User currentUser = getCurrentUser(); // from JWT
        PageRequest pageRequest = PageRequest.of(page, size);

        if (unread) {
            return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(currentUser, pageRequest);
        } else {
            return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser, pageRequest);
        }
    }

    // Count unread notifications
    public long getUnreadCountForCurrentUser() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    // Mark all notifications as read
    public void markAllAsReadForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(currentUser);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Delete one notification by id
    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);
    }

    // Create one mock notification
    public Notification createMock() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
	    Notification notification = new Notification();
	    notification.setNotificationId(UUID.randomUUID());
	    notification.setMessage("This is a test notification");
	    notification.setUser(user);
	    notification.setIsRead(false);

		// Send real-time notification via WebSocket
		webSocketService.sendNotificationToUser(user.getUsername(), notification);

		return notificationRepository.save(notification);
    }
}
