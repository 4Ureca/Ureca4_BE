package com.uplus.crm.domain.summary.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * GET /api/summaries 검색 파라미터
 *
 * <p>기본 검색: keyword, 상담 기간, 담당 상담사, 카테고리, 채널</p>
 * <p>상세 검색: IAM 3종, 고객 4종, 위험 유형 체크리스트, 상담사 이름</p>
 */
@Getter
public class SummarySearchRequest {

  // ── 기본 검색 ──────────────────────────────────────────────────────────────

  /** 통합 키워드 (iam.issue / action / memo / summary.content / keywords 전체 OR 검색) */
  private String keyword;

  /** 상담 시작일 (포함) */
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate from;

  /** 상담 종료일 (포함) */
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate to;

  /** 담당 상담사 ID */
  private Long agentId;

  /** 상담 카테고리 코드 (예: M_FEE_01) */
  private String categoryCode;

  /** 상담 채널 (PHONE / CHAT) */
  private String channel;

  // ── IAM 기반 상세 검색 ──────────────────────────────────────────────────────

  /** 상담 키워드 — iam.issue 부분 검색 */
  private String iamIssue;

  /** 상담 조치사항 — iam.action 부분 검색 */
  private String iamAction;

  /** 상담 특이사항 — iam.memo 부분 검색 */
  private String iamMemo;

  // ── 고객 기반 상세 검색 ─────────────────────────────────────────────────────

  /** 고객 이름 (부분 일치, 성 제외 이름만 입력 가능) */
  private String customerName;

  /** 고객 연락처 (부분 일치) */
  private String customerPhone;

  /** 고객 유형 (개인 / 법인) */
  private String customerType;

  /** 고객 등급 — 복수 선택 (VVIP / VIP / DIAMOND) */
  private List<String> customerGrades;

  // ── 위험 유형 체크리스트 ────────────────────────────────────────────────────

  /**
   * 위험 유형 — 복수 선택, OR 조건 적용
   * 예: [폭언/욕설, 해지위험, 반복민원, 사기의심, 정책악용, 과도한 보상 요구, 피싱피해]
   */
  private List<String> riskTypes;

  // ── 상담사 이름 검색 ────────────────────────────────────────────────────────

  /** 상담사 이름 부분 검색 */
  private String agentName;
}
