package com.uplus.crm.domain.notification.repository;

import com.uplus.crm.domain.notification.entity.UserNotificationSettings;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, Integer> {

    Optional<UserNotificationSettings> findByEmployeeEmpId(int empId);

    /**
     * 공지(notify_notice=true) 수신 대상 empId 목록.
     * targetRole: 'ALL' | 'AGENT' | 'ADMIN'
     */
    @Query("""
            SELECT s.employee.empId FROM UserNotificationSettings s
            WHERE s.notifyNotice = true
              AND (:targetRole = 'ALL'
                   OR (:targetRole = 'AGENT'
                       AND s.employee.employeeDetail.jobRole.roleName = '상담사')
                   OR (:targetRole = 'ADMIN'
                       AND s.employee.employeeDetail.jobRole.roleName = '관리자'))
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
