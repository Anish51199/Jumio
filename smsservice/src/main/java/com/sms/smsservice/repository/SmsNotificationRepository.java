package com.sms.smsservice.repository;


import com.sms.smsservice.entity.SMSNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsNotificationRepository extends JpaRepository<SMSNotification, Long> {
    List<SMSNotification> findByUserId(String userId);
}

