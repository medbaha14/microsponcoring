package com.example.microsponsoringbackend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.microsponsoringbackend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	List<Notification> findByUser_UserId(UUID userId);
}
