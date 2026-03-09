package com.uplus.crm.domain.elasticsearch.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MySQL(consultation_raw_texts) → Elasticsearch(consult-index) 동기화 서비스.
 *
 * <h3>파이프라인</h3>
 * <pre>
 * MySQL consultation_results  → consultId, iamIssue/Action/Memo, createdAt
 * MySQL consultation_raw_texts → raw_text_json
 *         ↓ extractPlainTextFromJson()   → rawText (전체 대화, 검색용)
 *         ↓ extractAgentTextFromJson()   → agentText (상담사 발화, 응대품질 감지용)
 * MongoDB consultation_summary → sentiment, riskFlags, customer, summary.keywords
 *         ↓
 * ConsultDoc 조립 → saveConsultation(doc, agentText)
 *         ↓ hasGreeting / hasFarewell 감지 (상담사 발화 기준)
 *         ↓
 * Elasticsearch consult-index 저장
 * </pre>
 *
 * <h3>응대품질 분석 정확도 개선 포인트</h3>
 * <ul>
 *   <li>기존: AI 요약 텍스트(content, allText) 기반 → 오탐 발생</li>
 *   <li>개선: 실제 상담사 발화(agentText)만 추출하여 인삿말·마무리 인사 감지</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultIndexSyncService {

    private static final int BATCH_SIZE = 100;

    private final SummaryConsultationResultRepository consultationResultRepository;
    private final ConsultationRawTextRepository rawTextRepository;
    private final SummaryRepository summaryRepository;
    private final ConsultSearchService consultSearchService;

    public record SyncResult(int total, int synced, int skipped, List<Long> failed) {
        public String summary() {
            return "total=%d, synced=%d, skipped(rawText없음)=%d, failed=%d"
                    .formatted(total, synced, skipped, failed.size());
        }
    }

    // ── 공개 API ─────────────────────────────────────────────────────────

    /**
     * MySQL의 모든 consultation_results를 ES에 동기화.
     * consultation_raw_texts가 없는 건은 skip.
     * 동일 consultId로 재실행 시 ES 문서를 덮어쓴다 (upsert).
     */
    @Transactional(readOnly = true)
    public SyncResult syncAll() {
        int pageNum = 0;
        int totalSynced = 0, totalSkipped = 0;
        List<Long> totalFailed = new ArrayList<>();
        int grandTotal = 0;

        Page<ConsultationResult> page;
        do {
            page = consultationResultRepository.findAll(PageRequest.of(pageNum++, BATCH_SIZE));
            List<ConsultationResult> batch = page.getContent();
            grandTotal += batch.size();

            SyncResult batchResult = syncBatch(batch);
            totalSynced += batchResult.synced();
            totalSkipped += batchResult.skipped();
            totalFailed.addAll(batchResult.failed());

        } while (page.hasNext());

        SyncResult result = new SyncResult(grandTotal, totalSynced, totalSkipped, totalFailed);
        log.info("[ConsultIndexSync] syncAll 완료 — {}", result.summary());
        return result;
    }

    /**
     * 단일 consultId를 ES에 동기화.
     * consultation_raw_texts가 없으면 skipped=1 반환.
     */
    @Transactional(readOnly = true)
    public SyncResult syncOne(Long consultId) {
        ConsultationResult result = consultationResultRepository.findById(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND));
        return syncBatch(List.of(result));
    }

    // ── private ───────────────────────────────────────────────────────────

    /**
     * 배치 단위 동기화.
     * rawText가 없는 건을 효율적으로 처리하기 위해 IN 쿼리로 일괄 조회.
     */
    private SyncResult syncBatch(List<ConsultationResult> results) {
        if (results.isEmpty()) return new SyncResult(0, 0, 0, List.of());

        List<Long> consultIds = results.stream()
                .map(ConsultationResult::getConsultId).toList();

        // rawText 일괄 조회 (없는 건은 skip 대상)
        Map<Long, String> rawTextMap = rawTextRepository.findByConsultIdIn(consultIds).stream()
                .collect(Collectors.toMap(
                        ConsultationRawText::getConsultId,
                        ConsultationRawText::getRawTextJson,
                        (a, b) -> a));  // consultId 중복 시 첫 번째 사용

        int synced = 0, skipped = 0;
        List<Long> failed = new ArrayList<>();

        for (ConsultationResult result : results) {
            Long consultId = result.getConsultId();

            String rawTextJson = rawTextMap.get(consultId);
            if (rawTextJson == null) {
                log.debug("[ConsultIndexSync] rawText 없음, 건너뜀: consultId={}", consultId);
                skipped++;
                continue;
            }

            try {
                // 전체 대화 평문 (검색용 rawText 필드)
                String rawText = consultSearchService.extractPlainTextFromJson(rawTextJson);
                // 상담사 발화만 (응대품질 hasGreeting/hasFarewell 감지용)
                String agentText = consultSearchService.extractAgentTextFromJson(rawTextJson);

                // MongoDB에서 AI 요약 정보 조회 (없어도 기본값으로 진행)
                Optional<ConsultationSummary> summaryOpt = summaryRepository.findByConsultId(consultId);
                ConsultationSummary summary = summaryOpt.orElse(null);
                if (summary == null) {
                    log.debug("[ConsultIndexSync] MongoDB 요약 없음, RDB만으로 동기화: consultId={}", consultId);
                }

                ConsultDoc doc = buildConsultDoc(result, summary, rawText);
                // agentText 기반으로 hasGreeting/hasFarewell 정확하게 감지
                consultSearchService.saveConsultation(doc, agentText);
                synced++;

            } catch (Exception e) {
                log.error("[ConsultIndexSync] 동기화 실패: consultId={}, error={}", consultId, e.getMessage(), e);
                failed.add(consultId);
            }
        }

        return new SyncResult(results.size(), synced, skipped, failed);
    }

    /**
     * MySQL + MongoDB 데이터를 조합하여 ConsultDoc 조립.
     * MongoDB 요약이 없으면 RDB 필드만으로 부분 구성.
     */
    private ConsultDoc buildConsultDoc(ConsultationResult result,
                                       ConsultationSummary summary,
                                       String rawText) {
        ConsultationSummary.Customer customer = summary != null ? summary.getCustomer() : null;
        ConsultationSummary.Summary sum       = summary != null ? summary.getSummary()  : null;

        int riskScore = deriveRiskScore(summary);

        return ConsultDoc.builder()
                // consultId를 ES 문서 ID로 사용 → 재동기화 시 upsert (중복 방지)
                .id(result.getConsultId().toString())
                .consultId(result.getConsultId())
                // AI 요약 내용 (MongoDB)
                .content(sum != null ? sum.getContent() : null)
                // IAM 필드 (RDB)
                .iamIssue(result.getIamIssue())
                .iamAction(result.getIamAction())
                .iamMemo(result.getIamMemo())
                // 통합 검색용 텍스트
                .allText(buildAllText(result, sum))
                // 대화 원문 평문 (전체 발화, 검색용)
                .rawText(rawText)
                // 감정·위험도 (MongoDB 기반 파생)
                .sentiment(deriveSentiment(summary))
                .riskScore(riskScore)
                .priority(derivePriority(riskScore))
                // 고객 정보
                .customerId(customer != null && customer.get_id() != null
                        ? customer.get_id().toString()
                        : String.valueOf(result.getCustomerId()))
                .customerName(customer != null ? customer.getName() : null)
                .phone(customer != null ? customer.getPhone() : null)
                .createdAt(result.getCreatedAt())
                .build();
    }

    /** IAM 필드 + 요약 키워드를 합쳐 allText 구성 (검색 coverage 극대화) */
    private String buildAllText(ConsultationResult result, ConsultationSummary.Summary sum) {
        StringBuilder sb = new StringBuilder();
        if (result.getIamIssue()  != null) sb.append(result.getIamIssue()).append(' ');
        if (result.getIamAction() != null) sb.append(result.getIamAction()).append(' ');
        if (result.getIamMemo()   != null) sb.append(result.getIamMemo()).append(' ');
        if (sum != null && sum.getKeywords() != null) {
            sb.append(String.join(" ", sum.getKeywords()));
        }
        return sb.toString().trim();
    }

    /** MongoDB customer.satisfiedScore → POSITIVE / NEGATIVE / NEUTRAL 변환 */
    private String deriveSentiment(ConsultationSummary summary) {
        if (summary == null || summary.getCustomer() == null) return "NEUTRAL";
        Double score = summary.getCustomer().getSatisfiedScore();
        if (score == null) return "NEUTRAL";
        if (score >= 4.0) return "POSITIVE";
        if (score <= 2.5) return "NEGATIVE";
        return "NEUTRAL";
    }

    /** MongoDB riskFlags → riskScore 수치 변환 */
    private int deriveRiskScore(ConsultationSummary summary) {
        if (summary == null || summary.getRiskFlags() == null || summary.getRiskFlags().isEmpty()) {
            return 0;
        }
        int max = 0;
        for (String flag : summary.getRiskFlags()) {
            if (flag.contains("해지위험") || flag.contains("법적")) max = Math.max(max, 85);
            else if (flag.contains("반복민원") || flag.contains("민원위협"))    max = Math.max(max, 65);
            else                                                                max = Math.max(max, 40);
        }
        return max;
    }

    /** riskScore → priority 문자열 변환 */
    private String derivePriority(int riskScore) {
        if (riskScore >= 85) return "URGENT";
        if (riskScore >= 65) return "HIGH";
        if (riskScore >= 35) return "NORMAL";
        return "LOW";
    }
}
