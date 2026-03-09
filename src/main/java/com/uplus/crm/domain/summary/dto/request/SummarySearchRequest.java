package com.uplus.crm.domain.summary.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * GET /api/summaries 검색 파라미터 (V26 필터 구성 기준)
 *
 * <p><b>Search Type (ES 활용, 동의어·추천어 지원)</b></p>
 * <ul>
 *   <li>{@code keyword}       : 자율검색 — 상담내용/상품명 전체 OR (ES → consultId 조인)</li>
 *   <li>{@code consultantName}: 담당 상담사 이름 부분 일치</li>
 *   <li>{@code customerName}  : 고객 이름 부분 일치</li>
 *   <li>{@code productName}   : 상품명 부분 일치 (resultProducts 배열 검색)</li>
 * </ul>
 *
 * <p><b>Toggle Type (DB/MongoDB 조건절)</b></p>
 * <ul>
 *   <li>{@code from} / {@code to}  : 상담 기간 (yyyy-MM-dd)</li>
 *   <li>{@code categoryName}       : 상담 카테고리명 (large/medium/small OR)</li>
 *   <li>{@code channel}            : 상담 채널 (CALL / CHATTING)</li>
 *   <li>{@code customerPhone}      : 고객 연락처 부분 일치</li>
 *   <li>{@code customerType}       : 고객 유형 (개인 / 법인)</li>
 *   <li>{@code customerGrades}     : 고객 등급 복수 선택 (VVIP, VIP, DIAMOND)</li>
 *   <li>{@code riskTypes}          : 위험 유형 복수 선택, OR 조건</li>
 *   <li>{@code satisfactionScore}  : 고객만족도 최소값 (1~5, 이상 검색)</li>
 * </ul>
 */
@Getter
@Setter
public class SummarySearchRequest {

    // ── Search Type (ES 활용) ───────────────────────────────────────────────

    /** 자율검색 — ES 동의어·추천어 적용, consultId 조인 후 MongoDB 필터 */
    private String keyword;

    /** 담당 상담사 이름 부분 검색 (agent.name) */
    private String consultantName;

    /** 고객 이름 부분 검색 (customer.name) */
    private String customerName;

    /** 상품명 부분 검색 (resultProducts.subscribed / canceled 배열) */
    private String productName;

    // ── Toggle Type (기간) ──────────────────────────────────────────────────

    /** 상담 시작일 (포함) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    /** 상담 종료일 (포함) */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    // ── Toggle Type (정형 조건) ─────────────────────────────────────────────

    /** 상담 카테고리명 — category.large / medium / small OR 검색 */
    private String categoryName;

    /** 상담 채널 — CALL 또는 CHATTING */
    private String channel;

    /** 고객 연락처 부분 일치 */
    private String customerPhone;

    /** 고객 유형 — 개인 또는 법인 */
    private String customerType;

    /** 고객 등급 복수 선택 — IN 조건 (VVIP / VIP / DIAMOND) */
    private List<String> customerGrades;

    /** 위험 유형 복수 선택 — riskFlags 배열 OR 조건 */
    private List<String> riskTypes;

    /** 고객만족도 최소값 (1~5) — customer.satisfiedScore >= satisfactionScore */
    private Integer satisfactionScore;
}
