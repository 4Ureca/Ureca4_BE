package com.uplus.crm.domain.consultation.repository;

import com.uplus.crm.domain.consultation.dto.response.ConsultationListItemDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationListQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ConsultationListItemDto> findConsultationList(
            String keyword,
            String channel,
            String categoryCode,
            String summaryStatus,
            String resultStatus,
            int offset,
            int size
    ) {
        String sql = """
                SELECT
                    cr.consult_id AS consult_id,
                    CONCAT('#CS-', DATE_FORMAT(cr.created_at, '%Y-'), LPAD(cr.consult_id, 6, '0')) AS consultation_number,
                    c.name AS customer_name,
                    c.phone AS customer_phone,
                    COALESCE(ccp.medium_category, ccp.large_category, cr.category_code) AS consultation_type,
                    CASE
                        WHEN cr.channel = 'CALL' THEN '전화'
                        WHEN cr.channel = 'CHATTING' THEN '채팅'
                        ELSE cr.channel
                    END AS channel_name,
                    e.name AS counselor_name,
                    CASE
                        WHEN res.status = 'PROCESSING' THEN '처리중'
                        WHEN res.status = 'COMPLETED' THEN '완료'
                        WHEN res.status = 'FAILED' THEN '미완료'
                        WHEN res.status = 'REQUESTED' THEN '요청중'
                        ELSE '-'
                    END AS process_status,
                    CASE
                        WHEN ses.status = 'requested' THEN '요청중'
                        WHEN ses.status = 'completed' THEN '요약완료'
                        WHEN ses.status = 'failed' THEN '실패'
                        ELSE '-'
                    END AS summary_status,
                    COALESCE(rv.satisfaction, 0) AS satisfaction,
                    DATE_FORMAT(cr.created_at, '%Y.%m.%d %H:%i') AS consulted_at
                FROM consultation_results cr
                JOIN customers c ON cr.customer_id = c.customer_id
                JOIN employees e ON cr.emp_id = e.emp_id
                LEFT JOIN consultation_category_policy ccp ON cr.category_code = ccp.category_code
                LEFT JOIN (
                    SELECT t1.consult_id, t1.status
                    FROM summary_event_status t1
                    INNER JOIN (
                        SELECT consult_id, MAX(created_at) AS max_created_at
                        FROM summary_event_status
                        GROUP BY consult_id
                    ) t2 ON t1.consult_id = t2.consult_id AND t1.created_at = t2.max_created_at
                ) ses ON cr.consult_id = ses.consult_id
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
                    SELECT consult_id, 
                           ROUND(AVG(score)) AS satisfaction
                    FROM (
                        SELECT consult_id, score_1 AS score FROM client_review WHERE deleted_at IS NULL
                        UNION ALL SELECT consult_id, score_2 FROM client_review WHERE deleted_at IS NULL
                        UNION ALL SELECT consult_id, score_3 FROM client_review WHERE deleted_at IS NULL
                        UNION ALL SELECT consult_id, score_4 FROM client_review WHERE deleted_at IS NULL
                        UNION ALL SELECT consult_id, score_5 FROM client_review WHERE deleted_at IS NULL
                    ) t WHERE score IS NOT NULL
                    GROUP BY consult_id
                ) rv ON cr.consult_id = rv.consult_id
                WHERE 1=1
                """;

        StringBuilder dynamicSql = new StringBuilder(sql);
        List<Object> params = new ArrayList<>();

        applyFilters(dynamicSql, params, keyword, channel, categoryCode, summaryStatus, resultStatus);

        dynamicSql.append(" ORDER BY cr.created_at DESC LIMIT ? OFFSET ? ");
        params.add(size);
        params.add(offset);

        Query query = entityManager.createNativeQuery(dynamicSql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return mapToDto(rows);
    }

    public long countConsultationList(
            String keyword, String channel, String categoryCode, String summaryStatus, String resultStatus
    ) {
        // COUNT 쿼리에서도 필터링을 위해 JOIN이 반드시 필요합니다.
        String sql = """
                SELECT COUNT(*)
                FROM consultation_results cr
                JOIN customers c ON cr.customer_id = c.customer_id
                LEFT JOIN (
                    SELECT t1.consult_id, t1.status
                    FROM summary_event_status t1
                    INNER JOIN (
                        SELECT consult_id, MAX(created_at) AS max_created_at
                        FROM summary_event_status
                        GROUP BY consult_id
                    ) t2 ON t1.consult_id = t2.consult_id AND t1.created_at = t2.max_created_at
                ) ses ON cr.consult_id = ses.consult_id
                LEFT JOIN (
                    SELECT t1.consult_id, t1.status
                    FROM result_event_status t1
                    INNER JOIN (
                        SELECT consult_id, MAX(created_at) AS max_created_at
                        FROM result_event_status
                        GROUP BY consult_id
                    ) t2 ON t1.consult_id = t2.consult_id AND t1.created_at = t2.max_created_at
                ) res ON cr.consult_id = res.consult_id
                WHERE 1=1
                """;

        StringBuilder dynamicSql = new StringBuilder(sql);
        List<Object> params = new ArrayList<>();

        applyFilters(dynamicSql, params, keyword, channel, categoryCode, summaryStatus, resultStatus);

        Query query = entityManager.createNativeQuery(dynamicSql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        return ((Number) query.getSingleResult()).longValue();
    }

    private void applyFilters(StringBuilder sql, List<Object> params, String keyword, String channel, String categoryCode, String summaryStatus, String resultStatus) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (c.name LIKE ? OR c.phone LIKE ? OR CAST(cr.consult_id AS CHAR) LIKE ?) ");
            String k = "%" + keyword + "%";
            params.add(k); params.add(k); params.add(k);
        }
        if (channel != null && !channel.isBlank()) {
            sql.append(" AND cr.channel = ? ");
            params.add(channel);
        }
        if (categoryCode != null && !categoryCode.isBlank()) {
            sql.append(" AND cr.category_code = ? ");
            params.add(categoryCode);
        }
        if (summaryStatus != null && !summaryStatus.isBlank()) {
            sql.append(" AND ses.status = ? ");
            params.add(summaryStatus);
        }
        if (resultStatus != null && !resultStatus.isBlank()) {
            sql.append(" AND res.status = ? ");
            params.add(resultStatus);
        }
    }

    private List<ConsultationListItemDto> mapToDto(List<Object[]> rows) {
        List<ConsultationListItemDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(ConsultationListItemDto.builder()
                    .consultId(row[0] == null ? null : ((Number) row[0]).longValue())
                    .consultationNumber((String) row[1])
                    .customerName((String) row[2])
                    .customerPhone((String) row[3])
                    .consultationType((String) row[4])
                    .channel((String) row[5])
                    .counselorName((String) row[6])
                    .processStatus((String) row[7])
                    .summaryStatus((String) row[8])
                    .satisfaction(row[9] == null ? 0 : ((Number) row[9]).intValue())
                    .consultedAt((String) row[10])
                    .build());
        }
        return result;
    }
}