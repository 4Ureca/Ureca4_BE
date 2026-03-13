package com.uplus.crm.domain.extraction.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.entity.ConsultationEvaluation;

@Repository
public interface ConsultationEvaluationRepository extends JpaRepository<ConsultationEvaluation, Long> {

    @Query("""
        SELECT new com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse(
            e.consultId, 
            p.smallCategory, 
            emp.name, 
            e.score, 
            a.rawSummary, 
            e.selectionStatus, 
            e.createdAt
        )
        FROM ConsultationEvaluation e
        JOIN ConsultationResult r ON e.consultId = r.consultId
        JOIN Employee emp ON r.empId = emp.empId
        JOIN ConsultationCategoryPolicy p ON r.categoryCode = p.categoryCode
        JOIN RetentionAnalysis a ON e.consultId = a.consultId
        AND (:status IS NULL OR CAST(e.selectionStatus AS string) = :status)
        """)
    Page<EvaluationListResponse> findCandidatePage(@Param("status") String status, Pageable pageable);
    
    @Query("""
	    SELECT new com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse(
	        e.consultId, 
	        emp.name, 
	        p.smallCategory, 
	        e.score, 
	        e.evaluationReason, 
	        a.rawSummary, 
	        e.selectionStatus, 
	        e.createdAt,
	        raw.rawTextJson
	    )
	    FROM ConsultationEvaluation e
	    JOIN ConsultationResult r ON e.consultId = r.consultId
	    JOIN Employee emp ON r.empId = emp.empId
	    JOIN ConsultationCategoryPolicy p ON r.categoryCode = p.categoryCode
	    JOIN RetentionAnalysis a ON e.consultId = a.consultId
	    JOIN ConsultationRawText raw ON e.consultId = raw.consultId
	    WHERE e.consultId = :consultId
    	""")
    Optional<EvaluationDetailResponse> findDetailByConsultId(@Param("consultId") Long consultId);
}