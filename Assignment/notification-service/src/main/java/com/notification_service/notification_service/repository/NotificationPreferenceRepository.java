package com.notification_service.notification_service.repository;

import com.notification_service.notification_service.entity.NotificationPreference;
import com.notification_service.notification_service.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {
    List<NotificationPreference> findByUserId(String userId);
    @Query("SELECT np FROM NotificationPreference np WHERE np.userId = :userId AND :channel MEMBER OF np.enabledChannels")
    Optional<NotificationPreference> findByUserIdAndChannel(@Param("userId") String userId, @Param("channel") NotificationChannel channel);
}

