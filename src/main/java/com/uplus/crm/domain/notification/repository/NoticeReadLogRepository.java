package com.uplus.crm.domain.notification.repository;

import com.uplus.crm.domain.notification.entity.NoticeReadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeReadLogRepository extends JpaRepository<NoticeReadLog, Long> {

    boolean existsByNoticeIdAndEmpId(int noticeId, int empId);
}
