package com.uplus.crm.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import com.uplus.crm.domain.notification.dto.NotificationSettingsResponse;
import com.uplus.crm.domain.notification.entity.NotificationType;
import com.uplus.crm.domain.notification.entity.UserNotification;
import com.uplus.crm.domain.notification.entity.UserNotificationSettings;
import com.uplus.crm.domain.notification.repository.UserNotificationRepository;
import com.uplus.crm.domain.notification.repository.UserNotificationSettingsRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock private UserNotificationRepository notificationRepository;
    @Mock private UserNotificationSettingsRepository settingsRepository;
    @Mock private EmployeeRepository employeeRepository;

    // ── sendNoticeAlert ───────────────────────────────────────────────────────

    @Test
    @DisplayName("sendNoticeAlert - URGENT 타입이면 URGENT 알림이 생성된다")
    void sendNoticeAlert_urgent_createsUrgentNotifications() {
        Employee employee = Employee.builder().empId(1).name("직원").build();
        Notice notice = Notice.builder()
                .title("긴급공지")
                .content("내용")
                .employee(employee)
                .noticeType(NoticeType.URGENT)
                .targetRole(TargetRole.ALL)
                .status(NoticeStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", 5);

        given(settingsRepository.findEmpIdsForNoticeAlert("ALL")).willReturn(List.of(1, 2, 3));
        given(notificationRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        notificationService.sendNoticeAlert(notice);

        then(notificationRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("sendNoticeAlert - 수신 대상이 없으면 저장하지 않는다")
    void sendNoticeAlert_noRecipients_skipsInsert() {
        Employee employee = Employee.builder().empId(1).name("직원").build();
        Notice notice = Notice.builder()
                .title("공지")
                .content("내용")
                .employee(employee)
                .noticeType(NoticeType.GENERAL)
                .targetRole(TargetRole.AGENT)
                .status(NoticeStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", 1);

        given(settingsRepository.findEmpIdsForNoticeAlert("AGENT")).willReturn(List.of());

        notificationService.sendNoticeAlert(notice);

        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("sendNoticeAlert - POLICY 타입이면 findEmpIdsForPolicyAlert를 호출한다")
    void sendNoticeAlert_policy_usesPolicyAlert() {
        Employee employee = Employee.builder().empId(1).name("직원").build();
        Notice notice = Notice.builder()
                .title("정책변경")
                .content("내용")
                .employee(employee)
                .noticeType(NoticeType.POLICY)
                .targetRole(TargetRole.ALL)
                .status(NoticeStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", 2);

        given(settingsRepository.findEmpIdsForPolicyAlert("ALL")).willReturn(List.of(10));
        given(notificationRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        notificationService.sendNoticeAlert(notice);

        then(settingsRepository).should().findEmpIdsForPolicyAlert("ALL");
        then(settingsRepository).shouldHaveNoMoreInteractions();
    }

    // ── readNotification ──────────────────────────────────────────────────────

    @Test
    @DisplayName("readNotification - 다른 직원의 알림이면 FORBIDDEN_ACCESS 예외")
    void readNotification_wrongEmpId_throws() {
        UserNotification notification = UserNotification.builder()
                .empId(99)
                .notificationType(NotificationType.NOTICE)
                .message("공지 알림")
                .build();
        ReflectionTestUtils.setField(notification, "notificationId", 1L);

        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.readNotification(1L, 1))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN_ACCESS));
    }

    @Test
    @DisplayName("readNotification - 존재하지 않는 알림이면 NOTIFICATION_NOT_FOUND 예외")
    void readNotification_notFound_throws() {
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.readNotification(999L, 1))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    // ── toggleSetting ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("toggleSetting - notifyNotice를 토글하면 값이 반전된다")
    void toggleSetting_notifyNotice_toggles() {
        Employee employee = Employee.builder().empId(1).name("직원").build();
        UserNotificationSettings settings = UserNotificationSettings.builder()
                .employee(employee)
                .notifyNotice(true)
                .build();

        given(settingsRepository.findByEmployeeEmpId(1)).willReturn(Optional.of(settings));

        NotificationSettingsResponse response =
                notificationService.toggleSetting(1, "notice");

        assertThat(response.notifyNotice()).isFalse();
    }

    @Test
    @DisplayName("toggleSetting - 설정이 없으면 기본값으로 새로 생성한다")
    void toggleSetting_noSettings_createsDefault() {
        Employee employee = Employee.builder().empId(1).name("직원").build();
        UserNotificationSettings defaultSettings =
                UserNotificationSettings.defaultOf(employee);

        given(settingsRepository.findByEmployeeEmpId(1)).willReturn(Optional.empty());
        given(employeeRepository.findById(1)).willReturn(Optional.of(employee));
        given(settingsRepository.save(any())).willReturn(defaultSettings);

        NotificationSettingsResponse response =
                notificationService.toggleSetting(1, "policy_change");

        // 기본값 false → toggle → true
        assertThat(response.notifyPolicyChange()).isTrue();
    }
}
