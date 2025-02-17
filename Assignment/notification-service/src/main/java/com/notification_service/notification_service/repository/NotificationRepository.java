package com.notification_service.notification_service.repository;

import com.notification_service.notification_service.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserId(String userId); // Retrieve notifications by user

    @Query("SELECT n FROM NotificationEntity n WHERE n.scheduledTime <= :now AND n.status = 'PENDING'")
    List<NotificationEntity> findDueNotifications(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = 'PROCESSING' WHERE n.id = :id AND n.status = 'PENDING'")
    int lockNotification(@Param("id") Long id);
}

