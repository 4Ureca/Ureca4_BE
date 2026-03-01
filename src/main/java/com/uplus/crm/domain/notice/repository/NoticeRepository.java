package com.uplus.crm.domain.notice.repository;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.TargetRole;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    // ── 목록 조회 ──────────────────────────────────────────────────────────────

    // ADMIN: DELETED 제외 전체 — isPinned DESC → createdAt DESC
    Page<Notice> findAllByStatusNotOrderByIsPinnedDescCreatedAtDesc(
            NoticeStatus status, Pageable pageable);

    // AGENT: ACTIVE + target_role 필터 + visible 기간 체크
    @Query("""
            SELECT n FROM Notice n
            WHERE n.status = :status
              AND n.targetRole IN :roles
              AND (n.visibleFrom IS NULL OR n.visibleFrom <= :now)
              AND (n.visibleTo   IS NULL OR n.visibleTo   >= :now)
            ORDER BY n.isPinned DESC, n.createdAt DESC
            """)
    Page<Notice> findActiveForAgent(
            @Param("status") NoticeStatus status,
            @Param("roles")  List<TargetRole> roles,
            @Param("now")    LocalDateTime now,
            Pageable pageable);

    // ── 스케줄러 ───────────────────────────────────────────────────────────────

    // SCHEDULED → ACTIVE 대상: visible_from <= NOW
    List<Notice> findAllByStatusAndVisibleFromLessThanEqual(
            NoticeStatus status, LocalDateTime now);

    // ACTIVE → ARCHIVED 대상: visible_to < NOW
    List<Notice> findAllByStatusAndVisibleToLessThan(
            NoticeStatus status, LocalDateTime now);
}
