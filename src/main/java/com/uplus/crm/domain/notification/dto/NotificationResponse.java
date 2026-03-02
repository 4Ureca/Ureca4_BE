package com.uplus.crm.domain.notification.dto;

import com.uplus.crm.domain.notification.entity.NotificationType;
import com.uplus.crm.domain.notification.entity.UserNotification;
import java.time.LocalDateTime;

public record NotificationResponse(
        long notificationId,
        NotificationType notificationType,
        Long refId,
        String message,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(UserNotification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getNotificationType(),
                n.getRefId(),
                n.getMessage(),
                n.isRead(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
