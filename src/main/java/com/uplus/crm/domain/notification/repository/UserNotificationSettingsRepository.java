package com.uplus.crm.domain.notification.repository;

import com.uplus.crm.domain.notification.entity.UserNotificationSettings;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationSettingsRepository
        extends JpaRepository<UserNotificationSettings, Integer> {

    Optional<UserNotificationSettings> findByEmployeeEmpId(int empId);

    /**
     * 공지(notify_notice) 수신 대상 empId 목록.
     * notify_notice 기본값이 true이므로 설정 레코드가 없는 직원도 수신 대상에 포함.
     * 명시적으로 notify_notice=false 설정한 직원만 제외(blocklist 방식).
     * targetRole: 'ALL' | 'AGENT' | 'ADMIN'
     */
    @Query("""
            SELECT e.empId FROM Employee e
            LEFT JOIN e.employeeDetail ed
            LEFT JOIN ed.jobRole jr
            WHERE (
              :targetRole = 'ALL'
              OR (:targetRole = 'AGENT' AND jr.roleName = '상담사')
              OR (:targetRole = 'ADMIN' AND jr.roleName = '관리자')
            )
            AND e.empId NOT IN (
              SELECT s.employee.empId FROM UserNotificationSettings s
              WHERE s.notifyNotice = false
            )
            """)
    List<Integer> findEmpIdsForNoticeAlert(@Param("targetRole") String targetRole);

    /**
     * 운영정책 변경(notify_policy_change=true) 수신 대상 empId 목록.
     */
    @Query("""
            SELECT s.employee.empId FROM UserNotificationSettings s
            WHERE s.notifyPolicyChange = true
              AND (:targetRole = 'ALL'
                   OR (:targetRole = 'AGENT'
                       AND s.employee.employeeDetail.jobRole.roleName = '상담사')
                   OR (:targetRole = 'ADMIN'
                       AND s.employee.employeeDetail.jobRole.roleName = '관리자'))
            """)
    List<Integer> findEmpIdsForPolicyAlert(@Param("targetRole") String targetRole);
}
