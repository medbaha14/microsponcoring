package com.example.microsponsoringbackend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.microsponsoringbackend.model.Notification;
import com.example.microsponsoringbackend.model.User;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	List<Notification> findByUser_UserId(UUID userId);
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    long countByUserAndIsReadFalse(User user);
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
}
