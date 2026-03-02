package com.uplus.crm.domain.notice.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.request.NoticeUpdateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeListResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeSummary;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import com.uplus.crm.domain.notice.repository.NoticeRepository;
import com.uplus.crm.domain.notification.entity.NoticeReadLog;
import com.uplus.crm.domain.notification.repository.NoticeReadLogRepository;
import com.uplus.crm.domain.notification.repository.UserNotificationRepository;
import com.uplus.crm.domain.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final EmployeeRepository employeeRepository;
    private final NoticeReadLogRepository noticeReadLogRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationService notificationService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request, int empId) {
        validateVisiblePeriod(request.visibleFrom(), request.visibleTo());

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        NoticeStatus status = determineStatus(request.visibleFrom());

        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .employee(employee)
                .isPinned(request.isPinned())
                .noticeType(request.noticeType() != null ? request.noticeType() : NoticeType.GENERAL)
                .targetRole(request.targetRole() != null ? request.targetRole() : TargetRole.ALL)
                .status(status)
                .visibleFrom(request.visibleFrom())
                .visibleTo(request.visibleTo())
                .build();

        Notice saved = noticeRepository.save(notice);

        if (request.sendNotification()) {
            notificationService.sendNoticeAlert(saved);
        }

        return NoticeResponse.from(saved);
    }

    // ── READ: 목록 (역할별 필터) ───────────────────────────────────────────────

    /**
     * ADMIN: DELETED 제외 전체 (DRAFT/SCHEDULED 포함)<br>
     * AGENT: ACTIVE + target_role IN(ALL, AGENT) + visible 기간 유효
     */
    public Page<NoticeSummary> getNoticeList(int empId, String roleName, Pageable pageable) {
        if ("관리자".equals(roleName)) {
            return noticeRepository
                    .findAllByStatusNotOrderByIsPinnedDescCreatedAtDesc(NoticeStatus.DELETED, pageable)
                    .map(NoticeSummary::from);
        }
        // AGENT
        return noticeRepository
                .findActiveForAgent(
                        NoticeStatus.ACTIVE,
                        List.of(TargetRole.ALL, TargetRole.AGENT),
                        LocalDateTime.now(),
                        pageable)
                .map(NoticeSummary::from);
    }

    // ── READ: 상세 + 조회수 증가 + 읽음 처리 ───────────────────────────────────

    @Transactional
    public NoticeResponse getNoticeDetail(int noticeId, int empId, String roleName) {
        Notice notice = getNoticeOrThrow(noticeId);

        // 0. 상담사가 ADMIN 전용 공지에 접근하는 경우 차단
        if (!"관리자".equals(roleName) && notice.getTargetRole() == TargetRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 1. 조회수 증가
        notice.increaseViewCount();

        // 2. 읽음 이력 INSERT (중복 skip)
        if (!noticeReadLogRepository.existsByNoticeIdAndEmpId(noticeId, empId)) {
            noticeReadLogRepository.save(
                    NoticeReadLog.builder()
                            .noticeId(noticeId)
                            .empId(empId)
                            .build()
            );
        }

        // 3. 해당 공지 알림 읽음 처리
        userNotificationRepository.markNoticeAlertAsRead(empId, (long) noticeId);

        return NoticeResponse.from(notice);
    }

    // ── READ: 단순 조회 (기존 호환용) ─────────────────────────────────────────

    @Transactional
    public NoticeResponse getNotice(Integer noticeId) {
        Notice notice = getNoticeOrThrow(noticeId);
        notice.increaseViewCount();
        return NoticeResponse.from(notice);
    }

    // ── 페이징 목록 (기존 호환용) ──────────────────────────────────────────────

    public NoticeListResponse getNotices(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "page는 0 이상, size는 1 이상이어야 합니다.");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notice> noticePage = noticeRepository
                .findAllByStatusNotOrderByIsPinnedDescCreatedAtDesc(NoticeStatus.DELETED, pageRequest);

        List<NoticeSummary> content = noticePage.getContent().stream()
                .map(NoticeSummary::from)
                .toList();

        return NoticeListResponse.builder()
                .content(content)
                .totalElements(noticePage.getTotalElements())
                .totalPages(noticePage.getTotalPages())
                .page(noticePage.getNumber())
                .size(noticePage.getSize())
                .build();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public NoticeResponse updateNotice(Integer noticeId, NoticeUpdateRequest request) {
        validateVisiblePeriod(request.visibleFrom(), request.visibleTo());

        Notice notice = getNoticeOrThrow(noticeId);
        if (notice.getStatus() == NoticeStatus.DELETED) {
            throw new BusinessException(ErrorCode.NOTICE_ALREADY_DELETED);
        }

        notice.update(
                request.title(),
                request.content(),
                request.isPinned(),
                request.noticeType(),
                request.targetRole(),
                determineStatus(request.visibleFrom()),
                request.visibleFrom(),
                request.visibleTo()
        );
        return NoticeResponse.from(notice);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteNotice(int noticeId, int empId) {
        Notice notice = getNoticeOrThrow(noticeId);
        // 컨트롤러에서 @PreAuthorize("hasRole('ADMIN')")로 권한이 보장됨
        notice.softDelete();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Notice getNoticeOrThrow(int noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    /**
     * visibleFrom 기준 상태 결정:
     * null 또는 과거/현재 → ACTIVE, 미래 → SCHEDULED
     */
    private NoticeStatus determineStatus(LocalDateTime visibleFrom) {
        if (visibleFrom == null || !visibleFrom.isAfter(LocalDateTime.now())) {
            return NoticeStatus.ACTIVE;
        }
        return NoticeStatus.SCHEDULED;
    }

    private void validateVisiblePeriod(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "노출 시작 일시는 노출 종료 일시보다 늦을 수 없습니다.");
        }
    }
}
