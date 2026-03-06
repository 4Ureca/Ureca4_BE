package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SummaryService {

  private final SummaryRepository summaryRepository;
  private final SummaryConsultationResultRepository consultationResultRepository;
  private final MongoTemplate mongoTemplate;

  /**
   * 복합 조건 검색 — MongoDB Criteria 기반
   *
   * <ul>
   *   <li>기본 검색: keyword, 상담 기간, 담당 상담사, 카테고리, 채널</li>
   *   <li>IAM 기반: issue / action / memo 부분 검색</li>
   *   <li>고객 기반: 이름(부분), 연락처(부분), 유형, 등급(복수)</li>
   *   <li>위험 유형: riskFlags 포함 여부 (OR)</li>
   *   <li>상담사 이름: 부분 검색</li>
   * </ul>
   */
  public Page<ConsultationSummaryListResponse> search(
      SummarySearchRequest req, Pageable pageable) {

    Criteria criteria = buildCriteria(req);
    Query query = new Query(criteria).with(pageable);
    long total = mongoTemplate.count(new Query(criteria), ConsultationSummary.class);
    List<ConsultationSummary> content =
        mongoTemplate.find(query, ConsultationSummary.class);

    return new PageImpl<>(
        content.stream().map(ConsultationSummaryListResponse::from).toList(),
        pageable,
        total);
  }

  public ConsultationSummaryDetailResponse getDetail(Long id) {
    if (!consultationResultRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);
    }

    ConsultationSummary entity =
        summaryRepository.findByConsultId(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));

    return ConsultationSummaryDetailResponse.from(entity);
  }

  // ── private ────────────────────────────────────────────────────────────────

  private Criteria buildCriteria(SummarySearchRequest req) {
    List<Criteria> conditions = new ArrayList<>();

    // 통합 키워드: issue / action / memo / summary.content / keywords (OR)
    if (StringUtils.hasText(req.getKeyword())) {
      Pattern kw = iPattern(req.getKeyword());
      conditions.add(new Criteria().orOperator(
          Criteria.where("iam.issue").regex(kw),
          Criteria.where("iam.action").regex(kw),
          Criteria.where("iam.memo").regex(kw),
          Criteria.where("summary.content").regex(kw),
          Criteria.where("summary.keywords").regex(kw)
      ));
    }

    // 상담 기간
    if (req.getFrom() != null || req.getTo() != null) {
      Criteria date = Criteria.where("consultedAt");
      if (req.getFrom() != null) date = date.gte(req.getFrom().atStartOfDay());
      if (req.getTo()   != null) date = date.lte(req.getTo().atTime(LocalTime.MAX));
      conditions.add(date);
    }

    // 담당 상담사 ID (정확 일치)
    if (req.getAgentId() != null) {
      conditions.add(Criteria.where("agent._id").is(req.getAgentId()));
    }

    // 담당 상담사 이름 (부분 검색)
    if (StringUtils.hasText(req.getAgentName())) {
      conditions.add(Criteria.where("agent.name").regex(iPattern(req.getAgentName())));
    }

    // 카테고리 코드 (정확 일치)
    if (StringUtils.hasText(req.getCategoryCode())) {
      conditions.add(Criteria.where("category.code").is(req.getCategoryCode()));
    }

    // 상담 채널
    if (StringUtils.hasText(req.getChannel())) {
      conditions.add(Criteria.where("channel").is(req.getChannel()));
    }

    // IAM 기반 검색 (각 필드 부분 일치)
    if (StringUtils.hasText(req.getIamIssue())) {
      conditions.add(Criteria.where("iam.issue").regex(iPattern(req.getIamIssue())));
    }
    if (StringUtils.hasText(req.getIamAction())) {
      conditions.add(Criteria.where("iam.action").regex(iPattern(req.getIamAction())));
    }
    if (StringUtils.hasText(req.getIamMemo())) {
      conditions.add(Criteria.where("iam.memo").regex(iPattern(req.getIamMemo())));
    }

    // 고객 이름 (부분 일치 — 성 제외 이름만 입력해도 검색 가능)
    if (StringUtils.hasText(req.getCustomerName())) {
      conditions.add(Criteria.where("customer.name").regex(iPattern(req.getCustomerName())));
    }

    // 고객 연락처 (부분 일치)
    if (StringUtils.hasText(req.getCustomerPhone())) {
      conditions.add(Criteria.where("customer.phone").regex(iPattern(req.getCustomerPhone())));
    }

    // 고객 유형 (정확 일치)
    if (StringUtils.hasText(req.getCustomerType())) {
      conditions.add(Criteria.where("customer.type").is(req.getCustomerType()));
    }

    // 고객 등급 (복수 선택 — IN 조건)
    if (req.getCustomerGrades() != null && !req.getCustomerGrades().isEmpty()) {
      conditions.add(Criteria.where("customer.grade").in(req.getCustomerGrades()));
    }

    // 위험 유형 (복수 선택 — riskFlags 배열에 하나라도 포함, OR)
    if (req.getRiskTypes() != null && !req.getRiskTypes().isEmpty()) {
      conditions.add(Criteria.where("riskFlags").in(req.getRiskTypes()));
    }

    if (conditions.isEmpty()) {
      return new Criteria();
    }
    return new Criteria().andOperator(conditions.toArray(new Criteria[0]));
  }

  /** 대소문자 무시 Pattern (한국어에서는 실질적 효과 없지만 영문 혼용 필드에 대응) */
  private static Pattern iPattern(String text) {
    return Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
  }
}