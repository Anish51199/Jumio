package com.notification_service.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "userfcm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmToken {
    @Id
    private String userId;
    private String fcmToken;
}
