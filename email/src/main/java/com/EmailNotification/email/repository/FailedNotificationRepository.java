package com.EmailNotification.email.repository;

import com.EmailNotification.email.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {
    List<FailedNotification> findByUserId(String userId);

    // Optional: Query to find failed notifications by email (if needed)
    List<FailedNotification> findByEmail(String email);

    // Optional: Query to find failed notifications by error message (if needed)
    List<FailedNotification> findByErrorMessageContaining(String errorMessage);
}

