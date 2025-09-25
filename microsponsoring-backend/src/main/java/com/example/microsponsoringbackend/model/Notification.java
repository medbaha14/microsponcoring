package com.example.microsponsoringbackend.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
	private UUID notificationId;

	@Column(nullable = false, length = 500)
	private String message;

	@Column(nullable = false)
	private Boolean isRead = false;

	@Column(nullable = false)
	private Date createdAt = new Date();

	@Column(nullable = false)
	private Date updatedAt = new Date();

	// Recipient
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@PrePersist
	protected void onCreate() {
		Date now = new Date();
		this.createdAt = now;
		this.updatedAt = now; // also set updatedAt on insert
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}
}
