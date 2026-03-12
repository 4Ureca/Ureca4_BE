package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.CustomerRepository.SubscribedProductProjection;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/summaries/{consultId} 상세 조회 응답.
 *
 * <p>MongoDB(AI 요약), RDB(원문·고객·상품), 병합(Merge) 전략:
 * <ul>
 *   <li>MongoDB 데이터가 있으면 우선 반환</li>
 *   <li>MongoDB에 없는 필드는 RDB로 보완</li>
 *   <li>MongoDB 자체가 없어도 RDB 데이터만으로 부분 응답</li>
 * </ul>
 */
@Getter
@Builder
public class ConsultationSummaryDetailResponse {

    private Long consultId;
    private LocalDateTime consultedAt;
    private LocalDateTime createdAt;
    private Integer durationSec;
    private String channel;

    private CategoryInfo category;
    private AgentInfo agent;
    private CustomerInfo customer;
    private ContentInfo content;          // AI 요약 + 원문 스크립트
    private AnalysisInfo analysis;        // IAM + 위험유형 + 방어 분석
    private List<ActiveSubscriptionInfo> activeSubscriptions;  // RDB 현재 가입 상품

    // ── 내부 DTO ──────────────────────────────────────────────────────────

    @Getter @Builder
    public static class CategoryInfo {
        private String code;
        private String large;
        private String medium;
        private String small;
    }

    @Getter @Builder
    public static class AgentInfo {
        private Long id;
        private String name;
        private String department;        // RDB EmployeeDetail → Department
    }

    @Getter @Builder
    public static class CustomerInfo {
        private String name;
        private String phone;
        private String type;
        private String grade;
        private String ageGroup;
        /** 고객만족도 (1~5, null 이면 "미작성") */
        private String satisfaction;
    }

    /** AI 요약 텍스트 + 상담 원문 스크립트 */
    @Getter @Builder
    public static class ContentInfo {
        private String status;
        private String aiSummary;
        private List<String> keywords;
        /** RDB consultation_raw_texts.raw_text_json — 상담 원문 스크립트 */
        private String rawTextJson;
    }

    /** IAM 지표 + 위험유형 + 해지 방어 분석 */
    @Getter @Builder
    public static class AnalysisInfo {
        private String iamIssue;
        private String iamAction;
        private String iamMemo;
        private Double iamMatchRate;
        private List<String> riskFlags;

        private Boolean cancellationIntent;
        private Boolean defenseAttempted;
        private Boolean defenseSuccess;
        private List<String> defenseActions;
        private String complaintReasons;
    }

    /** RDB UNION ALL 쿼리로 조회한 현재 가입 상품 */
    @Getter @Builder
    public static class ActiveSubscriptionInfo {
        private String productType;   // HOME / MOBILE / ADDITIONAL
        private String productCode;
        private String productName;
        private String category;
    }

    // ── 정적 팩토리 ────────────────────────────────────────────────────────

    /**
     * RDB + MongoDB 데이터를 병합하여 응답 객체를 생성한다.
     *
     * @param result          RDB consultation_results (필수)
     * @param summary         MongoDB consultation_summary (없으면 null)
     * @param customer        RDB customers (없으면 null)
     * @param rawText         RDB consultation_raw_texts (없으면 null)
     * @param category        RDB consultation_category_policy (없으면 null)
     * @param activeProducts  RDB 현재 가입 상품 목록
     * @param employee        RDB employees + employee_details (없으면 null)
     */
    public static ConsultationSummaryDetailResponse merge(
            ConsultationResult result,
            ConsultationSummary summary,
            Customer customer,
            ConsultationRawText rawText,
            ConsultationCategoryPolicy category,
            List<SubscribedProductProjection> activeProducts,
            Employee employee) {

        return ConsultationSummaryDetailResponse.builder()
                .consultId(result.getConsultId())
                .consultedAt(resolveConsultedAt(result, summary))
                .createdAt(result.getCreatedAt())
                .durationSec(result.getDurationSec())
                .channel(result.getChannel())
                .category(buildCategory(result, summary, category))
                .agent(buildAgent(summary, employee))
                .customer(buildCustomer(summary, customer))
                .content(buildContent(summary, rawText, result))
                .analysis(buildAnalysis(summary, result))
                .activeSubscriptions(buildActiveSubscriptions(activeProducts))
                .build();
    }

    // ── private 헬퍼 ───────────────────────────────────────────────────────

    private static LocalDateTime resolveConsultedAt(
            ConsultationResult result, ConsultationSummary summary) {
        if (summary != null && summary.getConsultedAt() != null) {
            return summary.getConsultedAt();
        }
        return result.getCreatedAt(); // fallback
    }

    private static CategoryInfo buildCategory(
            ConsultationResult result,
            ConsultationSummary summary,
            ConsultationCategoryPolicy rdbCategory) {

        // MongoDB 우선, 없으면 RDB ConsultationCategoryPolicy
        if (summary != null && summary.getCategory() != null) {
            return CategoryInfo.builder()
                    .code(summary.getCategory().getCode())
                    .large(summary.getCategory().getLarge())
                    .medium(summary.getCategory().getMedium())
                    .small(summary.getCategory().getSmall())
                    .build();
        }
        if (rdbCategory != null) {
            return CategoryInfo.builder()
                    .code(result.getCategoryCode())
                    .large(rdbCategory.getLargeCategory())
                    .medium(rdbCategory.getMediumCategory())
                    .small(rdbCategory.getSmallCategory())
                    .build();
        }
        return CategoryInfo.builder().code(result.getCategoryCode()).build();
    }

    private static AgentInfo buildAgent(ConsultationSummary summary, Employee employee) {
        Long mongoId = (summary != null && summary.getAgent() != null)
                ? summary.getAgent().get_id() : null;
        String mongoName = (summary != null && summary.getAgent() != null)
                ? summary.getAgent().getName() : null;

        String dept = null;
        if (employee != null && employee.getEmployeeDetail() != null
                && employee.getEmployeeDetail().getDepartment() != null) {
            dept = employee.getEmployeeDetail().getDepartment().getDeptName();
        }

        return AgentInfo.builder()
                .id(mongoId != null ? mongoId : (employee != null ? employee.getEmpId().longValue() : null))
                .name(mongoName != null ? mongoName : (employee != null ? employee.getName() : null))
                .department(dept)
                .build();
    }

    private static CustomerInfo buildCustomer(
            ConsultationSummary summary, Customer rdbCustomer) {

        // MongoDB 우선
        if (summary != null && summary.getCustomer() != null) {
            ConsultationSummary.Customer mc = summary.getCustomer();
            Double score = mc.getSatisfiedScore();
            return CustomerInfo.builder()
                    .name(mc.getName())
                    .phone(mc.getPhone())
                    .type(mc.getType())
                    .grade(mc.getGrade())
                    .ageGroup(mc.getAgeGroup())
                    .satisfaction(score == null ? "미작성" : String.valueOf(score))
                    .build();
        }
        // RDB fallback
        if (rdbCustomer != null) {
            return CustomerInfo.builder()
                    .name(rdbCustomer.getName())
                    .phone(rdbCustomer.getPhone())
                    .type(rdbCustomer.getCustomerType())
                    .grade(rdbCustomer.getGradeCode())
                    .build();
        }
        return null;
    }

    private static ContentInfo buildContent(
            ConsultationSummary summary,
            ConsultationRawText rawText,
            ConsultationResult result) {

        String rawJson = (rawText != null) ? rawText.getRawTextJson() : null;

        if (summary != null && summary.getSummary() != null) {
            return ContentInfo.builder()
                    .status(summary.getSummary().getStatus())
                    .aiSummary(summary.getSummary().getContent())
                    .keywords(summary.getSummary().getKeywords())
                    .rawTextJson(rawJson)
                    .build();
        }
        // MongoDB 없으면 RDB IAM 텍스트만 반환
        return ContentInfo.builder()
                .rawTextJson(rawJson)
                .build();
    }

    private static AnalysisInfo buildAnalysis(
            ConsultationSummary summary, ConsultationResult result) {

        if (summary != null) {
            return AnalysisInfo.builder()
                    .iamIssue(summary.getIam() != null ? summary.getIam().getIssue()
                            : result.getIamIssue())
                    .iamAction(summary.getIam() != null ? summary.getIam().getAction()
                            : result.getIamAction())
                    .iamMemo(summary.getIam() != null ? summary.getIam().getMemo()
                            : result.getIamMemo())
                    .iamMatchRate(summary.getIam() != null ? summary.getIam().getMatchRates() : null)
                    .riskFlags(summary.getRiskFlags())
                    .cancellationIntent(summary.getCancellation() != null
                            ? summary.getCancellation().getIntent() : null)
                    .defenseAttempted(summary.getCancellation() != null
                            ? summary.getCancellation().getDefenseAttempted() : null)
                    .defenseSuccess(summary.getCancellation() != null
                            ? summary.getCancellation().getDefenseSuccess() : null)
                    .defenseActions(summary.getCancellation() != null
                            ? summary.getCancellation().getDefenseActions() : null)
                    .complaintReasons(summary.getCancellation() != null
                            ? summary.getCancellation().getComplaintReasons() : null)
                    .build();
        }
        // MongoDB 없으면 RDB IAM 필드만
        return AnalysisInfo.builder()
                .iamIssue(result.getIamIssue())
                .iamAction(result.getIamAction())
                .iamMemo(result.getIamMemo())
                .build();
    }

    private static List<ActiveSubscriptionInfo> buildActiveSubscriptions(
            List<SubscribedProductProjection> projections) {
        if (projections == null || projections.isEmpty()) return List.of();
        return projections.stream()
                .map(p -> ActiveSubscriptionInfo.builder()
                        .productType(p.getProductType())
                        .productCode(p.getProductCode())
                        .productName(p.getProductName())
                        .category(p.getCategory())
                        .build())
                .toList();
    }
}
