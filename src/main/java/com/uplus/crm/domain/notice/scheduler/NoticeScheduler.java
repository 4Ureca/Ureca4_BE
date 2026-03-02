package com.uplus.crm.domain.notice.scheduler;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.repository.NoticeRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지 상태 자동 전환 스케줄러.
 * <ul>
 *   <li>매분 실행: SCHEDULED → ACTIVE (visibleFrom ≤ now)</li>
 *   <li>매일 자정: ACTIVE → ARCHIVED (visibleTo &lt; now)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeScheduler {

    private final NoticeRepository noticeRepository;

    /**
     * 매분 0초에 실행 — 노출 시작 시각이 도래한 SCHEDULED 공지를 ACTIVE로 전환한다.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void activateScheduledNotices() {
        List<Notice> notices = noticeRepository
                .findAllByStatusAndVisibleFromLessThanEqual(
                        NoticeStatus.SCHEDULED, LocalDateTime.now());

        notices.forEach(notice -> notice.updateStatus(NoticeStatus.ACTIVE));

        if (!notices.isEmpty()) {
            log.info("[Scheduler] SCHEDULED→ACTIVE 처리: {}건", notices.size());
        }
    }

    /**
     * 매일 자정에 실행 — 노출 종료 시각이 지난 ACTIVE 공지를 ARCHIVED로 전환한다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void archiveExpiredNotices() {
        List<Notice> notices = noticeRepository
                .findAllByStatusAndVisibleToLessThan(
                        NoticeStatus.ACTIVE, LocalDateTime.now());

        notices.forEach(notice -> notice.updateStatus(NoticeStatus.ARCHIVED));

        if (!notices.isEmpty()) {
            log.info("[Scheduler] ACTIVE→ARCHIVED 처리: {}건", notices.size());
        }
    }
}
