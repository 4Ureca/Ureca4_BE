package com.uplus.crm.domain.extraction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.response.WeeklyExcellentCaseResponse;
import com.uplus.crm.domain.extraction.entity.WeeklyExcellentCase;

public interface WeeklyExcellentCaseRepository extends JpaRepository<WeeklyExcellentCase, Long> {
	@Query("""
	        SELECT new com.uplus.crm.domain.extraction.dto.response.WeeklyExcellentCaseResponse(
	            w.snapshotId,
	            w.consultId,
	            emp.name,
	            p.smallCategory,
	            CONCAT('[', w.yearVal, '년 ', w.weekVal, '주차] 우수 상담 사례'),
	            a.rawSummary,
	            e.score,
	            w.adminReason,
	            w.selectedAt
	        )
	        FROM WeeklyExcellentCase w
	        JOIN ConsultationEvaluation e ON w.evaluationId = e.evaluationId
	        JOIN ConsultationResult r ON w.consultId = r.consultId
	        JOIN Employee emp ON r.empId = emp.empId
	        JOIN ConsultationCategoryPolicy p ON r.categoryCode = p.categoryCode
	        JOIN RetentionAnalysis a ON w.consultId = a.consultId
	        WHERE (:year IS NULL OR w.yearVal = :year)
	          AND (:week IS NULL OR w.weekVal = :week)
	        ORDER BY w.selectedAt DESC
	        """)
	    List<WeeklyExcellentCaseResponse> findWeeklyCases(@Param("year") Integer year, @Param("week") Integer week);
	boolean existsByConsultId(Long consultId);
	
	@Modifying
    @Transactional 
	void deleteByConsultId(Long consultId);
}