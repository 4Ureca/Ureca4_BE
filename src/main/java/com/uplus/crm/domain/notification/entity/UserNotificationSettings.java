package com.uplus.crm.domain.notification.entity;

import com.uplus.crm.domain.account.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Integer settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "notify_notice", nullable = false)
    @Builder.Default
    private boolean notifyNotice = true;

    @Column(name = "notify_best_practice", nullable = false)
    @Builder.Default
    private boolean notifyBestPractice = true;

    @Column(name = "notify_policy_change", nullable = false)
    @Builder.Default
    private boolean notifyPolicyChange = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기본 설정으로 신규 생성.
     */
    public static UserNotificationSettings defaultOf(Employee employee) {
        return UserNotificationSettings.builder()
                .employee(employee)
                .build();
    }

    /**
     * 수신 설정 토글.
     *
     * @param field "notify_notice" | "notify_best_practice" | "notify_policy_change"
     */
    public void toggle(String field) {
        switch (field) {
            case "notify_notice"        -> this.notifyNotice        = !this.notifyNotice;
            case "notify_best_practice" -> this.notifyBestPractice  = !this.notifyBestPractice;
            case "notify_policy_change" -> this.notifyPolicyChange  = !this.notifyPolicyChange;
            default -> throw new IllegalArgumentException("알 수 없는 설정 필드: " + field);
        }
    }
}
