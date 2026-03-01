package com.uplus.crm.domain.notice.repository;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    Page<Notice> findAllByStatusNot(NoticeStatus status, Pageable pageable);
}
