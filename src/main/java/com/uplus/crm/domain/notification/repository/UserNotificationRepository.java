package com.uplus.crm.domain.notification.repository;

import com.uplus.crm.domain.notification.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    // 미읽음 알림 수
    long countByEmpIdAndIsReadFalse(int empId);

    // 알림 목록 (최신순)
    Page<UserNotification> findByEmpIdOrderByCreatedAtDesc(int empId, Pageable pageable);

    // 전체 미읽음 일괄 읽음 처리 (벌크)
    @Modifying
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.empId = :empId AND n.isRead = false")
    int markAllAsRead(@Param("empId") int empId);

    // 특정 공지 알림 읽음 처리 (공지 상세 조회 시 연동)
    @Modifying
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.empId = :empId AND n.refId = :refId " +
           "AND n.notificationType IN ('NOTICE','URGENT') AND n.isRead = false")
    int markNoticeAlertAsRead(@Param("empId") int empId, @Param("refId") long refId);
}
