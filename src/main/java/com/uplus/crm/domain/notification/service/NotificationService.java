package com.uplus.crm.domain.notification.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import com.uplus.crm.domain.notification.dto.NotificationResponse;
import com.uplus.crm.domain.notification.dto.NotificationSettingsResponse;
import com.uplus.crm.domain.notification.entity.NotificationType;
import com.uplus.crm.domain.notification.entity.UserNotification;
import com.uplus.crm.domain.notification.entity.UserNotificationSettings;
import com.uplus.crm.domain.notification.repository.UserNotificationRepository;
import com.uplus.crm.domain.notification.repository.UserNotificationSettingsRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int CHUNK_SIZE = 500;

    private final UserNotificationRepository notificationRepository;
    private final UserNotificationSettingsRepository settingsRepository;
    private final EmployeeRepository employeeRepository;

    // ── 공지 알림 발송 ─────────────────────────────────────────────────────────

    /**
     * 공지 등록 시 수신 설정을 확인하여 대상 직원에게 알림을 발송한다.
     * <ul>
     *   <li>URGENT / GENERAL / SYSTEM / EVENT → notify_notice = true 대상</li>
     *   <li>POLICY → notify_policy_change = true 대상</li>
     *   <li>target_role로 AGENT / ADMIN / ALL 필터 추가 적용</li>
     * </ul>
     */
    @Transactional
    public void sendNoticeAlert(Notice notice) {
        String targetRole = notice.getTargetRole() != null
                ? notice.getTargetRole().name()
                : TargetRole.ALL.name();

        List<Integer> empIds = resolveRecipients(notice.getNoticeType(), targetRole);
        if (empIds.isEmpty()) {
            log.info("[Notification] 수신 대상 없음 - noticeId={}", notice.getNoticeId());
            return;
        }

        NotificationType type    = resolveNotificationType(notice.getNoticeType());
        String           message = buildMessage(notice);
        long             refId   = notice.getNoticeId().longValue();

        List<UserNotification> notifications = empIds.stream()
                .map(empId -> UserNotification.builder()
                        .empId(empId)
                        .notificationType(type)
                        .refId(refId)
                        .message(message)
                        .build())
                .toList();

        saveInChunks(notifications);
        log.info("[Notification] 공지 알림 발송 완료 - noticeId={}, 수신자={}",
                notice.getNoticeId(), empIds.size());
    }

    // ── 단건 읽음 처리 ────────────────────────────────────────────────────────

    @Transactional
    public void readNotification(long notificationId, int empId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getEmpId() != empId) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        notification.markAsRead();
    }

    // ── 전체 읽음 처리 ────────────────────────────────────────────────────────

    @Transactional
    public void markAllAsRead(int empId) {
        int updated = notificationRepository.markAllAsRead(empId);
        log.debug("[Notification] 전체 읽음 처리 - empId={}, count={}", empId, updated);
    }

    // ── 알림 목록 조회 ────────────────────────────────────────────────────────

    public Page<NotificationResponse> getNotifications(int empId, Pageable pageable) {
        return notificationRepository
                .findByEmpIdOrderByCreatedAtDesc(empId, pageable)
                .map(NotificationResponse::from);
    }

    // ── 미읽음 뱃지 수 ────────────────────────────────────────────────────────

    public long getUnreadCount(int empId) {
        return notificationRepository.countByEmpIdAndIsReadFalse(empId);
    }

    // ── 알림 설정 조회 ────────────────────────────────────────────────────────

    @Transactional   // 설정이 없을 때 getOrCreateSettings()에서 save() 호출 — readOnly 오버라이드
    public NotificationSettingsResponse getSettings(int empId) {
        UserNotificationSettings settings = getOrCreateSettings(empId);
        return NotificationSettingsResponse.from(settings);
    }

    // ── 알림 설정 토글 ────────────────────────────────────────────────────────

    /**
     * @param field "notice" | "best_practice" | "policy_change"
     */
    @Transactional
    public NotificationSettingsResponse toggleSetting(int empId, String field) {
        UserNotificationSettings settings = getOrCreateSettings(empId);
        settings.toggle("notify_" + field);
        return NotificationSettingsResponse.from(settings);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private UserNotificationSettings getOrCreateSettings(int empId) {
        return settingsRepository.findByEmployeeEmpId(empId)
                .orElseGet(() -> {
                    Employee employee = employeeRepository.findById(empId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
                    return settingsRepository.save(UserNotificationSettings.defaultOf(employee));
                });
    }

    private List<Integer> resolveRecipients(NoticeType noticeType, String targetRole) {
        if (noticeType == NoticeType.POLICY) {
            return settingsRepository.findEmpIdsForPolicyAlert(targetRole);
        }
        return settingsRepository.findEmpIdsForNoticeAlert(targetRole);
    }

    private NotificationType resolveNotificationType(NoticeType noticeType) {
        return switch (noticeType) {
            case URGENT -> NotificationType.URGENT;
            case POLICY -> NotificationType.POLICY_CHANGE;
            case EVENT  -> NotificationType.EVENT;
            default     -> NotificationType.NOTICE;
        };
    }

    private String buildMessage(Notice notice) {
        String prefix = switch (notice.getNoticeType()) {
            case URGENT -> "[긴급] ";
            case SYSTEM -> "[시스템] ";
            case POLICY -> "[정책] ";
            case EVENT  -> "[이벤트] ";
            default     -> "[공지] ";
        };
        String raw = prefix + notice.getTitle();
        return raw.length() > 300 ? raw.substring(0, 300) : raw;
    }

    private void saveInChunks(List<UserNotification> all) {
        List<List<UserNotification>> chunks = partition(all, CHUNK_SIZE);
        chunks.forEach(notificationRepository::saveAll);
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
