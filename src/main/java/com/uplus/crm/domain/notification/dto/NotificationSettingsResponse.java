package com.uplus.crm.domain.notification.dto;

import com.uplus.crm.domain.notification.entity.UserNotificationSettings;
import java.time.LocalDateTime;

public record NotificationSettingsResponse(
        boolean notifyNotice,
        boolean notifyBestPractice,
        boolean notifyPolicyChange,
        LocalDateTime updatedAt
) {
    public static NotificationSettingsResponse from(UserNotificationSettings s) {
        return new NotificationSettingsResponse(
                s.isNotifyNotice(),
                s.isNotifyBestPractice(),
                s.isNotifyPolicyChange(),
                s.getUpdatedAt()
        );
    }
}
