package com.notification_service.notification_service.repository;

import com.notification_service.notification_service.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, String> {
}
