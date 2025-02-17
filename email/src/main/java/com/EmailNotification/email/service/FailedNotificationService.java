package com.EmailNotification.email.service;

import com.EmailNotification.email.entity.FailedNotification;
import com.EmailNotification.email.repository.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FailedNotificationService {

    private final FailedNotificationRepository failedNotificationRepository;

    // Get all failed notifications
    public List<FailedNotification> getAllFailedNotifications() {
        return failedNotificationRepository.findAll();
    }

    // Get failed notifications by userId
    public List<FailedNotification> getFailedNotificationsByUserId(String userId) {
        return failedNotificationRepository.findByUserId(userId);
    }

    // Get failed notifications by email
    public List<FailedNotification> getFailedNotificationsByEmail(String email) {
        return failedNotificationRepository.findByEmail(email);
    }

    // Get failed notifications by error message
    public List<FailedNotification> getFailedNotificationsByErrorMessage(String errorMessage) {
        return failedNotificationRepository.findByErrorMessageContaining(errorMessage);
    }
}

