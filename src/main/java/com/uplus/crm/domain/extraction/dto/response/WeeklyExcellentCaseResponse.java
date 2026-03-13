package com.uplus.crm.domain.extraction.dto.response;

import java.time.LocalDateTime;

public record WeeklyExcellentCaseResponse(
	    Long snapshotId,
	    Long consultId,
	    String counselorName,   // 상담사 성함
	    String smallCategory,   // 상담 카테고리
	    String title,           // 게시글 제목 (예: [11주차] 상담 사례)
	    String rawSummary,      // 상담 요약
	    Integer score,          // AI 점수
	    String adminReason,     // 관리자 선정 사유
	    LocalDateTime selectedAt
	) {}
