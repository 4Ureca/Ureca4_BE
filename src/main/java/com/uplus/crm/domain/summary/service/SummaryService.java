package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository.SubscribedProductProjection;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {

    // ── MongoDB ───────────────────────────────────────────────────────────
    private final SummaryRepository summaryRepository;
    private final MongoTemplate mongoTemplate;

    // ── RDB ───────────────────────────────────────────────────────────────
    private final SummaryConsultationResultRepository consultationResultRepository;
    private final CustomerRepository customerRepository;
    private final ConsultationRawTextRepository rawTextRepository;
    private final ConsultationCategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;

    // ── Elasticsearch ─────────────────────────────────────────────────────
    private final ConsultSearchService consultSearchService;

    // ─────────────────────────────────────────────────────────────────────
    // 목록 조회 (Phase 1) — Hybrid Search
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Hybrid 검색 — ES(keyword) + MongoDB(조건절) 결합.
     *
     * <ul>
     *   <li>keyword: ES → consultId 목록 → MongoDB IN 필터
     *       (ES consultId 없으면 MongoDB regex fallback)</li>
     *   <li>나머지 필터: MongoDB Criteria로 직접 처리</li>
     * </ul>
     */
    public Page<ConsultationSummaryListResponse> search(
            SummarySearchRequest req, Pageable pageable) {

        Criteria criteria = buildCriteria(req);
        Query query = new Query(criteria).with(pageable);
        long total = mongoTemplate.count(new Query(criteria), ConsultationSummary.class);
        List<ConsultationSummary> content = mongoTemplate.find(query, ConsultationSummary.class);

        return new PageImpl<>(
                content.stream().map(ConsultationSummaryListResponse::from).toList(),
                pageable,
                total);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 상세 조회 (Phase 2) — RDB + MongoDB 병렬 Merge
    // ─────────────────────────────────────────────────────────────────────

    /**
     * RDB + MongoDB 데이터를 CompletableFuture로 병렬 조회한 후 병합하여 반환한다.
     *
     * <p><b>조회 소스</b></p>
     * <ul>
     *   <li>RDB {@code consultation_results} — 필수 (없으면 404)</li>
     *   <li>MongoDB {@code consultation_summary} — 선택 (없으면 AI 필드 null, RDB로 부분 응답)</li>
     *   <li>RDB {@code customers}, {@code consultation_raw_texts},
     *       {@code consultation_category_policy}, 가입 상품 UNION,
     *       {@code employees + employee_details} — 병렬 fetch</li>
     * </ul>
     *
     * <p><b>병합 우선순위</b>: MongoDB 데이터 우선, 없으면 RDB 보완</p>
     */
    public ConsultationSummaryDetailResponse getDetail(Long id) {

        // 1. RDB consultation_results — 404 판단 기준
        ConsultationResult result = consultationResultRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND));

        Long    customerId = result.getCustomerId();
        Integer empId      = result.getEmpId();
        String  catCode    = result.getCategoryCode();

        // 2. 병렬 비동기 조회
        //    각 supplyAsync 호출은 독립 트랜잭션으로 처리되므로 읽기 안전.
        CompletableFuture<Optional<ConsultationSummary>> mongoFuture =
                CompletableFuture.supplyAsync(() -> summaryRepository.findByConsultId(id));

        CompletableFuture<Optional<Customer>> customerFuture =
                CompletableFuture.supplyAsync(() -> customerRepository.findById(customerId));

        CompletableFuture<Optional<ConsultationRawText>> rawTextFuture =
                CompletableFuture.supplyAsync(() -> rawTextRepository.findFirstByConsultId(id));

        CompletableFuture<List<SubscribedProductProjection>> productsFuture =
                CompletableFuture.supplyAsync(
                        () -> customerRepository.findActiveSubscribedProducts(customerId));

        CompletableFuture<Optional<ConsultationCategoryPolicy>> categoryFuture =
                CompletableFuture.supplyAsync(() -> categoryRepository.findById(catCode));

        CompletableFuture<Optional<Employee>> employeeFuture =
                CompletableFuture.supplyAsync(() -> employeeRepository.findByIdWithDetails(empId));

        // 3. 전체 완료 대기
        CompletableFuture.allOf(
                mongoFuture, customerFuture, rawTextFuture,
                productsFuture, categoryFuture, employeeFuture
        ).join();

        // 4. 결과 추출
        ConsultationSummary              summary   = mongoFuture.join().orElse(null);
        Customer                         customer  = customerFuture.join().orElse(null);
        ConsultationRawText              rawText   = rawTextFuture.join().orElse(null);
        List<SubscribedProductProjection> products = productsFuture.join();
        ConsultationCategoryPolicy       category  = categoryFuture.join().orElse(null);
        Employee                         employee  = employeeFuture.join().orElse(null);

        if (summary == null) {
            log.warn("[SummaryService] consultId={} MongoDB 요약 없음 — RDB 부분 응답 반환", id);
        }

        // 5. 병합 응답
        return ConsultationSummaryDetailResponse.merge(
                result, summary, customer, rawText, category, products, employee);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 추천 검색어
    // ─────────────────────────────────────────────────────────────────────

    /**
     * IAM 기반 검색 추천 키워드 반환.
     * {@code summary.keywords} 집계 후 부족하면 {@code iam.matchKeyword}로 보충.
     */
    public List<String> suggestKeywords(String prefix, int limit) {
        String safePrefix = (prefix != null && !prefix.isBlank())
                ? Pattern.quote(prefix.trim()) : null;

        Set<String> result = new LinkedHashSet<>();
        result.addAll(aggregateKeywordsFromField("summary.keywords", safePrefix, limit));

        if (result.size() < limit) {
            aggregateKeywordsFromField("iam.matchKeyword", safePrefix, limit - result.size())
                    .stream().filter(k -> !result.contains(k)).forEach(result::add);
        }
        return List.copyOf(result);
    }

    private List<String> aggregateKeywordsFromField(String field, String safePrefix, int n) {
        List<AggregationOperation> ops = new ArrayList<>();

        if (safePrefix != null) {
            ops.add(Aggregation.match(Criteria.where(field).regex("^" + safePrefix, "i")));
        } else {
            ops.add(Aggregation.match(Criteria.where(field).exists(true)));
        }
        ops.add(Aggregation.unwind("$" + field));
        if (safePrefix != null) {
            ops.add(Aggregation.match(Criteria.where(field).regex("^" + safePrefix, "i")));
        }
        ops.add(Aggregation.group(field).count().as("cnt"));
        ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "cnt")));
        ops.add(Aggregation.limit(n));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(ops), ConsultationSummary.class, Document.class)
                .getMappedResults().stream()
                .map(d -> d.getString("_id"))
                .filter(Objects::nonNull)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 검색 조건 빌더 (목록 조회용)
    // ─────────────────────────────────────────────────────────────────────

    private Criteria buildCriteria(SummarySearchRequest req) {
        List<Criteria> conditions = new ArrayList<>();

        // keyword — ES Hybrid (ES consultId 없으면 MongoDB regex fallback)
        if (StringUtils.hasText(req.getKeyword())) {
            List<Long> esIds = consultSearchService.searchConsultIdsByKeyword(req.getKeyword());
            if (!esIds.isEmpty()) {
                conditions.add(Criteria.where("consultId").in(esIds));
            } else {
                Pattern kw = iPattern(req.getKeyword());
                conditions.add(new Criteria().orOperator(
                        Criteria.where("iam.issue").regex(kw),
                        Criteria.where("iam.action").regex(kw),
                        Criteria.where("iam.memo").regex(kw),
                        Criteria.where("summary.content").regex(kw),
                        Criteria.where("summary.keywords").regex(kw)
                ));
            }
        }

        // 상담 기간
        if (req.getFrom() != null || req.getTo() != null) {
            Criteria date = Criteria.where("consultedAt");
            if (req.getFrom() != null) date = date.gte(req.getFrom().atStartOfDay());
            if (req.getTo()   != null) date = date.lte(req.getTo().atTime(LocalTime.MAX));
            conditions.add(date);
        }

        // 담당 상담사 이름 (부분 일치)
        if (StringUtils.hasText(req.getConsultantName())) {
            conditions.add(Criteria.where("agent.name").regex(iPattern(req.getConsultantName())));
        }

        // 상담 카테고리명 (large/medium/small OR)
        if (StringUtils.hasText(req.getCategoryName())) {
            Pattern cat = iPattern(req.getCategoryName());
            conditions.add(new Criteria().orOperator(
                    Criteria.where("category.large").regex(cat),
                    Criteria.where("category.medium").regex(cat),
                    Criteria.where("category.small").regex(cat)
            ));
        }

        // 상담 채널 (CALL / CHATTING)
        if (StringUtils.hasText(req.getChannel())) {
            conditions.add(Criteria.where("channel").is(req.getChannel()));
        }

        // 고객 이름 (부분 일치)
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

        // 고객 등급 (복수 — IN)
        if (req.getCustomerGrades() != null && !req.getCustomerGrades().isEmpty()) {
            conditions.add(Criteria.where("customer.grade").in(req.getCustomerGrades()));
        }

        // 위험 유형 (복수 — riskFlags 배열 OR)
        if (req.getRiskTypes() != null && !req.getRiskTypes().isEmpty()) {
            conditions.add(Criteria.where("riskFlags").in(req.getRiskTypes()));
        }

        // 상품명 (resultProducts subscribed/canceled 배열 OR)
        if (StringUtils.hasText(req.getProductName())) {
            Pattern prod = iPattern(req.getProductName());
            conditions.add(new Criteria().orOperator(
                    Criteria.where("resultProducts.subscribed").regex(prod),
                    Criteria.where("resultProducts.canceled").regex(prod)
            ));
        }

        // 고객만족도 최소값 (customer.satisfiedScore >= N)
        if (req.getSatisfactionScore() != null) {
            conditions.add(Criteria.where("customer.satisfiedScore")
                    .gte(req.getSatisfactionScore().doubleValue()));
        }

        if (conditions.isEmpty()) return new Criteria();
        return new Criteria().andOperator(conditions.toArray(new Criteria[0]));
    }

    private static Pattern iPattern(String text) {
        return Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
    }
}
