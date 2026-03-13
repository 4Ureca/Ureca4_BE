package com.uplus.crm.domain.summary.controller;

import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "④ 상담 검색")
@RestController
@RequestMapping("/summaries")
@RequiredArgsConstructor
public class SummaryController {

  private final SummaryService service;

  @Operation(
      tags = {"④ 상담 검색"},
      summary = "[Step 1] 검색어 자동완성",
      description = """
          검색창 입력 중 호출하는 자동완성 API입니다.

          **분류**: 단순 검색용 — ES 미사용, MongoDB 집계

          **데이터 소스 (MongoDB)**
          - `summary.keywords` : AI가 추출한 상담 키워드 (주 소스, 빈도 내림차순)
          - `iam.matchKeyword`  : 상담 중 실제 매칭된 키워드 (보조 소스)

          **파라미터**
          - `q`    : 입력 중인 prefix. 미입력 시 전체 인기 키워드 Top N 반환
          - `size` : 반환 개수 (기본 10, 최대 30)

          **사용 순서**: Step 1 → Step 2(목록) → Step 3(상세)
          """
  )
  @GetMapping("/suggest")
  public List<String> suggest(
      @Parameter(description = "검색어 prefix (미입력 시 인기 키워드 반환)", example = "해지")
      @RequestParam(name = "q", required = false) String q, // 수정: name 명시
      @Parameter(description = "반환 개수 (최대 30)", example = "10")
      @RequestParam(name = "size", defaultValue = "10") int size) { // 수정: name 명시

    return service.suggestKeywords(q, Math.min(size, 30));
  }

  @Operation(
      tags = {"④ 상담 검색"},
      summary = "[Step 2] 상담 목록 검색 (Hybrid Search)",
      description = """
          키워드 + 필터 조건으로 상담 요약 목록을 검색합니다.

          **분류**: 단순 검색용 — ES를 검색 엔진으로 사용
          keyword 입력 시: ES 동의어 검색 → consultId 조인 → MongoDB 필터
          keyword 미입력 시: MongoDB 조건절만 사용

          **[ES 적용] keyword 검색 (동의어·오타 허용)**
          - `keyword` : 자유 검색어 (AND 우선 → OR 보완 순으로 노출)
            예) "갤폰 해지" → 갤럭시+해지 모두 포함 문서 먼저, 하나만 포함 문서 후순위
          - 동의어 자동 확장: 갤폰→갤럭시, 번이→번호이동, 넷플→넷플릭스

          **[MongoDB 필터]**
          - `from` / `to`       : 상담 기간 (yyyy-MM-dd)
          - `consultantName`    : 담당 상담사 이름 부분 일치
          - `customerName`      : 고객 이름 부분 일치
          - `customerPhone`     : 고객 연락처 부분 일치
          - `customerType`      : 고객 유형 (개인 / 법인)
          - `customerGrades`    : 고객 등급 복수 선택 (VVIP, VIP, DIAMOND)
          - `categoryName`      : 상담 카테고리명 (대/중/소분류 OR 검색)
          - `channel`           : 상담 채널 (CALL / CHATTING)
          - `riskTypes`         : 위험 유형 복수 선택 (폭언/욕설, 해지위험, 반복민원 등)
          - `productName`       : 상품명 부분 일치 (가입/해지 상품)
          - `satisfactionScore` : 고객만족도 최소값 1~5

          **사용 순서**: Step 1(자동완성) → Step 2 → Step 3(상세)
          """
  )
  @GetMapping
  public Page<ConsultationSummaryListResponse> list(
      @ParameterObject @ModelAttribute SummarySearchRequest searchRequest,
      @ParameterObject @PageableDefault(
          size = 20,
          sort = "consultedAt",
          direction = Sort.Direction.DESC
      ) Pageable pageable) {

    return service.search(searchRequest, pageable);
  }

  @Operation(
      tags = {"④ 상담 검색"},
      summary = "[Step 3] 상담 상세 조회",
      description = """
          목록에서 선택한 상담의 전체 상세 정보를 반환합니다.

          **분류**: 단순 조회 — ES 미사용, RDB + MongoDB 병렬 조회

          **데이터 소스 (6개 병렬 조회)**
          | 소스 | 제공 데이터 |
          |------|------------|
          | RDB `consultation_results` | 기본 상담 정보 (필수, 없으면 404) |
          | RDB `customers` | 고객 프로필 |
          | RDB `consultation_raw_texts` | 상담 원문 스크립트 (rawTextJson) |
          | RDB `consultation_category_policy` | 카테고리명 |
          | RDB 가입 상품 UNION | HOME/MOBILE/ADDITIONAL 현재 가입 상품 |
          | MongoDB `consultation_summary` | AI 요약, IAM, 위험유형, 해지 분석 |

          MongoDB 데이터가 없어도 404 없이 RDB 기반 부분 응답 반환.

          **사용 순서**: Step 1(자동완성) → Step 2(목록) → Step 3
          """
  )
  @GetMapping("/{consultId}")
  public ConsultationSummaryDetailResponse detail(
      @Parameter(description = "상담 결과서 ID (consultation_results.consult_id)")
      @PathVariable("consultId") Long consultId) { // 수정: name 명시

    return service.getDetail(consultId);
  }
}