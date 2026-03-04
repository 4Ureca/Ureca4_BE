package com.uplus.crm.domain.notification.repository;

import com.uplus.crm.domain.notification.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    long countByEmpIdAndIsReadFalse(int empId);

    Page<UserNotification> findByEmpIdOrderByCreatedAtDesc(int empId, Pageable pageable);

    /**
     * 해당 직원의 미읽음 알림을 모두 읽음으로 표시한다.
     */
    @Modifying
    @Query("""
            UPDATE UserNotification n
               SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
             WHERE n.empId = :empId AND n.isRead = false
            """)
    int markAllAsRead(@Param("empId") int empId);

    /**
     * 특정 공지 알림을 읽음으로 표시한다 (공지 상세 조회 시 호출).
     */
    @Modifying
    @Query("""
            UPDATE UserNotification n
               SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
             WHERE n.empId = :empId
               AND n.refId  = :refId
               AND n.isRead = false
            """)
    int markNoticeAlertAsRead(@Param("empId") int empId, @Param("refId") long refId);
}
