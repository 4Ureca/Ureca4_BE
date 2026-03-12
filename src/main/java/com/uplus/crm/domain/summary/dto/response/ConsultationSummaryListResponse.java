package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryListResponse {

    /** 상담 번호 */
    private Long consultId;

    /** 상담 일시 */
    private LocalDateTime consultedAt;

    /** 상담 채널 (CALL / CHATTING) */
    private String channel;

    // ── 고객 정보 ──────────────────────────────────────────────────────────
    private String customerName;
    private String customerType;
    private String customerGrade;

    // ── 상담 카테고리 ──────────────────────────────────────────────────────
    private String categoryCode;
    private String categoryLarge;
    private String categoryMedium;
    private String categorySmall;

    // ── 상담사 정보 ────────────────────────────────────────────────────────
    private Long agentId;
    private String agentName;

    // ── 위험 유형 태그 ─────────────────────────────────────────────────────
    /** 위험 유형 태그 목록 (예: [해지위험, 반복민원]) */
    private List<String> riskFlags;

    // ── 요약 정보 ──────────────────────────────────────────────────────────
    /** 요약 미리보기 (summary.content 앞 150자) */
    private String summaryContent;

    /** 요약 처리 상태 (예: COMPLETED, PENDING, FAILED) */
    private String summaryStatus;

    // ── IAM 정보 ───────────────────────────────────────────────────────────
    /** IAM 매칭률 (iam.matchRates) */
    private Double iamMatchRate;

    // ── 해지 방어 여부 ─────────────────────────────────────────────────────
    /** 해지 방어 시도 여부 (cancellation.defenseAttempted) */
    private Boolean defenseAttempted;

    public static ConsultationSummaryListResponse from(ConsultationSummary e) {
        String preview = null;
        String summaryStatus = null;
        if (e.getSummary() != null) {
            String content = e.getSummary().getContent();
            preview = (content != null && content.length() > 150)
                    ? content.substring(0, 150) + "…"
                    : content;
            summaryStatus = e.getSummary().getStatus();
        }

        return ConsultationSummaryListResponse.builder()
                .consultId(e.getConsultId())
                .consultedAt(e.getConsultedAt())
                .channel(e.getChannel())
                .customerName(e.getCustomer() != null ? e.getCustomer().getName() : null)
                .customerType(e.getCustomer() != null ? e.getCustomer().getType() : null)
                .customerGrade(e.getCustomer() != null ? e.getCustomer().getGrade() : null)
                .categoryCode(e.getCategory() != null ? e.getCategory().getCode() : null)
                .categoryLarge(e.getCategory() != null ? e.getCategory().getLarge() : null)
                .categoryMedium(e.getCategory() != null ? e.getCategory().getMedium() : null)
                .categorySmall(e.getCategory() != null ? e.getCategory().getSmall() : null)
                .agentId(e.getAgent() != null ? e.getAgent().get_id() : null)
                .agentName(e.getAgent() != null ? e.getAgent().getName() : null)
                .riskFlags(e.getRiskFlags())
                .summaryContent(preview)
                .summaryStatus(summaryStatus)
                .iamMatchRate(e.getIam() != null ? e.getIam().getMatchRates() : null)
                .defenseAttempted(e.getCancellation() != null ? e.getCancellation().getDefenseAttempted() : null)
                .build();
    }
}
