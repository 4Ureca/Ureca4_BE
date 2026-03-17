package com.uplus.crm.domain.consultation.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class ConsultationDetailQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public BasicInfoRow findBasicInfo(Long consultId) {
        String sql = """
                SELECT
                    cr.customer_id,
                    cr.consult_id,
                    CONCAT('#CS-', DATE_FORMAT(cr.created_at, '%Y-'), LPAD(cr.consult_id, 6, '0')) AS consultation_number,
                    c.name AS customer_name,
                    c.phone AS customer_phone,
                    c.email AS customer_email,
                    CASE
                        WHEN cr.channel = 'CALL' THEN '전화'
                        WHEN cr.channel = 'CHATTING' THEN '채팅'
                        ELSE cr.channel
                    END AS channel_name,
                    COALESCE(ccp.medium_category, ccp.large_category, cr.category_code) AS consultation_type,
                    e.name AS counselor_name,
                    CASE
                        WHEN res.status = 'PROCESSING' THEN '처리중'
                        WHEN res.status = 'COMPLETED' THEN '완료'
                        WHEN res.status = 'FAILED' THEN '미완료'
                        WHEN res.status = 'REQUESTED' THEN '요청중'
                        ELSE '-'
                    END AS process_status,
                    COALESCE(rv.satisfaction, 0) AS satisfaction,
                    DATE_FORMAT(cr.created_at, '%Y.%m.%d %H:%i') AS consulted_at,
                    cr.duration_sec,
                    ccp.large_category,
                    ccp.medium_category,
                    ccp.small_category,
                    cr.iam_issue,
                    cr.iam_action,
                    cr.iam_memo,
                    cr.category_code,
                    c.grade_code
                FROM consultation_results cr
                JOIN customers c ON cr.customer_id = c.customer_id
                JOIN employees e ON cr.emp_id = e.emp_id
                LEFT JOIN consultation_category_policy ccp ON cr.category_code = ccp.category_code
                LEFT JOIN (
                    SELECT t1.consult_id, t1.status
                    FROM result_event_status t1
                    INNER JOIN (
                        SELECT consult_id, MAX(created_at) AS max_created_at
                        FROM result_event_status
                        GROUP BY consult_id
                    ) t2 ON t1.consult_id = t2.consult_id AND t1.created_at = t2.max_created_at
                ) res ON cr.consult_id = res.consult_id
                LEFT JOIN (
                    SELECT
                        consult_id,
                        ROUND((COALESCE(score_1, 0) + COALESCE(score_2, 0) + COALESCE(score_3, 0) + COALESCE(score_4, 0) + COALESCE(score_5, 0)) 
                        / NULLIF((CASE WHEN score_1 IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN score_2 IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN score_3 IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN score_4 IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN score_5 IS NOT NULL THEN 1 ELSE 0 END), 0)) AS satisfaction
                    FROM client_review
                    WHERE deleted_at IS NULL
                ) rv ON cr.consult_id = rv.consult_id
                WHERE cr.consult_id = ?
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, consultId);

        List<Object[]> results = query.getResultList();
        if (results.isEmpty()) return null;

        Object[] row = results.get(0);
        return new BasicInfoRow(
                row[0] == null ? null : ((Number) row[0]).longValue(),
                row[1] == null ? null : ((Number) row[1]).longValue(),
                (String) row[2], (String) row[3], (String) row[4], (String) row[5],
                (String) row[6], (String) row[7], (String) row[8], (String) row[9],
                row[10] == null ? 0 : ((Number) row[10]).intValue(), (String) row[11],
                row[12] == null ? 0 : ((Number) row[12]).intValue(), (String) row[13],
                (String) row[14], (String) row[15], (String) row[16], (String) row[17],
                (String) row[18], (String) row[19], (String) row[20]
        );
    }

    public AiAnalysisRow findAiAnalysis(Long consultId) {
        String sql = """
                SELECT
                    ce.consult_id,          -- [0]
                    ra.has_intent,          -- [1]
                    ra.complaint_reason,    -- [2]
                    ra.defense_attempted,   -- [3]
                    ra.defense_success,     -- [4]
                    CAST(ra.defense_actions AS CHAR), -- [5]
                    ra.raw_summary,         -- [6]
                    ce.evaluation_reason,   -- [7]
                    ce.outbound_call_result,-- [8]
                    ce.outbound_report      -- [9]
                FROM consultation_evaluations ce
                LEFT JOIN retention_analysis ra 
                  ON ce.consult_id = ra.consult_id 
                 AND ra.deleted_at IS NULL
                WHERE ce.consult_id = ?
                ORDER BY 
                    (CASE WHEN ce.evaluation_reason IS NOT NULL THEN 0 ELSE 1 END), -- 값이 있는 것을 최우선으로
                    ce.created_at DESC 
                LIMIT 1
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, consultId);

        List<Object[]> results = query.getResultList();
        if (results.isEmpty()) return null;

        Object[] row = results.get(0);
        return new AiAnalysisRow(
                row[0] == null ? null : ((Number) row[0]).longValue(),
                toBoolean(row[1]),
                (String) row[2],
                toBoolean(row[3]),
                toBoolean(row[4]),
                row[5] == null ? null : row[5].toString(),
                (String) row[6],
                (String) row[7],
                (String) row[8],
                (String) row[9]
        );
    }

    public List<HistoryRow> findHistory(Long consultId) {
        String sql = """
                SELECT
                    DATE_FORMAT(created_at, '%Y.%m.%d %H:%i') AS occurred_at,
                    contract_type, product_type, new_product_home, new_product_mobile, new_product_service,
                    canceled_product_home, canceled_product_mobile, canceled_product_service
                FROM consult_product_logs
                WHERE consult_id = ? AND deleted_at IS NULL
                ORDER BY created_at DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, consultId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<HistoryRow> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new HistoryRow(
                    (String) row[0], (String) row[1], (String) row[2],
                    row[3] == null ? null : row[3].toString(),
                    row[4] == null ? null : row[4].toString(),
                    row[5] == null ? null : row[5].toString(),
                    row[6] == null ? null : row[6].toString(),
                    row[7] == null ? null : row[7].toString(),
                    row[8] == null ? null : row[8].toString()
            ));
        }
        return result;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue() == 1;
        return Boolean.parseBoolean(value.toString());
    }

    public record BasicInfoRow(Long customerId, Long consultId, String consultationNumber, String customerName, String customerPhone, String customerEmail, String channel, String consultationType, String counselorName, String processStatus, Integer satisfaction, String consultedAt, Integer durationMinutes, String largeCategory, String mediumCategory, String smallCategory, String iamIssue, String iamAction, String iamMemo, String categoryCode, String gradeCode) {}
    public record AiAnalysisRow(Long consultId, Boolean hasIntent, String complaintReason, Boolean defenseAttempted, Boolean defenseSuccess, String defenseActions, String rawSummary, String evaluationReason, String outboundCallResult, String outboundReport) {}
    public record HistoryRow(String occurredAt, String contractType, String productType, String newProductHome, String newProductMobile, String newProductService, String canceledProductHome, String canceledProductMobile, String canceledProductService) {}
}