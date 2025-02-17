package com.sms.smsservice.repository;


import com.sms.smsservice.entity.FailedSMSNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedSmsNotificationRepository extends JpaRepository<FailedSMSNotification, Long> {
}

