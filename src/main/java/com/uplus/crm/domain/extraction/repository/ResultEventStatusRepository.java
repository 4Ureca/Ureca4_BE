package com.uplus.crm.domain.extraction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uplus.crm.domain.extraction.dto.response.FailedAnalysisDto;
import com.uplus.crm.domain.extraction.entity.ResultEventStatus;

public interface ResultEventStatusRepository extends JpaRepository<ResultEventStatus, Long> {
	List<ResultEventStatus> findAllByConsultIdIn(List<Long> consultIds);
	@Query("SELECT new com.uplus.crm.domain.extraction.dto.response.FailedAnalysisDto(" +
	       "r.consultId, r.categoryCode, " +
	       "r.status, r.failReason, r.retryCount, " +
	       "e.status, e.failReason, e.retryCount, " +
	       "CASE WHEN e.updatedAt IS NULL OR r.updatedAt > e.updatedAt THEN " +
	       "  FUNCTION('DATE_FORMAT', r.updatedAt, '%Y-%m-%d %H:%i:%s') " +
	       "  ELSE FUNCTION('DATE_FORMAT', e.updatedAt, '%Y-%m-%d %H:%i:%s') END) " +
	       "FROM ResultEventStatus r " +
	       "LEFT JOIN ExcellentEventStatus e ON r.consultId = e.consultId " + 
	       "WHERE (r.status = 'FAILED' AND r.retryCount >= 3) " +
	       "   OR (e.status = 'FAILED' AND e.retryCount >= 3) " +
	       "ORDER BY r.updatedAt DESC")
	    List<FailedAnalysisDto> findIntegratedFailedTasks();
}
