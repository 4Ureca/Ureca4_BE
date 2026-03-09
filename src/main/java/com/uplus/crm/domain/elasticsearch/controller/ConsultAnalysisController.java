package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.elasticsearch.dto.ConsultAnalysisResponse;
import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 응대품질·분석 전용 검색 컨트롤러.
 *
 * <p>ES 검색 결과(ConsultDoc)에 MySQL consultation_raw_texts.raw_text_json을 병합하여 반환한다.</p>
 */
@Tag(name = "③ ES 분석")
@RestController
@RequestMapping("/elasticsearch/consult")
@RequiredArgsConstructor
public class ConsultAnalysisController {

    private final ConsultSearchService consultSearchService;
    private final ConsultationRawTextRepository rawTextRepository;

    @Operation(
        summary = "[분석 Step 1] 응대품질 — 인삿말·마무리 누락 / 대화 품질 표현 검색",
        description = """
            상담사의 응대품질을 두 가지 방식으로 조회합니다.
            전제조건: POST /admin/es/sync 완료 (실제 대화원문 기반)

            [방식 1] 인삿말·마무리 boolean 필터
            인덱싱 시 상담사 발화에서 자동 감지한 값으로 필터링합니다.
            - hasGreeting=false                   → 인사말 없이 시작한 상담
            - hasFarewell=false                   → 마무리 인사 없이 종료한 상담
            - hasGreeting=false&hasFarewell=false → 둘 다 없는 최우선 관리 대상

            [방식 2] 대화원문 품질 키워드 검색 (keyword)
            실제 대화 내용을 analysis_synonyms.txt 동의어로 검색합니다.
            - keyword=친절응대   → 친절하다, 상냥하다 등이 등장한 상담
            - keyword=공감응대   → 공감하다, 충분히 이해합니다 등이 등장한 상담
            - keyword=불만감정   → 고객이 화나다, 짜증난다 등을 표현한 상담
            - keyword=대기안내   → 잠시만요, 잠시 기다려주세요 등이 등장한 상담

            [두 방식 조합]
            hasGreeting=false&keyword=불만감정 → 인사말 없이 불만 고객을 응대한 상담

            참조 DB: Elasticsearch consult-index (rawText.analysis 서브필드)
                     + MySQL consultation_raw_texts (rawTextJson 병합)
            """
    )
    @GetMapping("/analysis/quality")
    public ResponseEntity<List<ConsultAnalysisResponse>> analyzeQuality(
            @Parameter(description = "인사말 포함 여부 필터 (true/false, 생략 시 전체)", example = "false")
            @RequestParam(required = false) Boolean hasGreeting,
            @Parameter(description = "마무리 인사 포함 여부 필터 (true/false, 생략 시 전체)", example = "false")
            @RequestParam(required = false) Boolean hasFarewell,
            @Parameter(description = "대화원문 품질 키워드 (analysis_synonyms.txt 동의어 적용, 예: 친절응대, 불만감정, 대기안내)", example = "친절응대")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        List<ConsultDoc> docs = consultSearchService.searchByGreetingFlag(hasGreeting, hasFarewell, keyword, page, size);
        return ResponseEntity.ok(enrichWithRawText(docs));
    }

    @Operation(
        summary = "[분석 Step 2] 분석용 키워드 검색 — 응대 어근 제거 후 실질 내용 검색",
        description = """
            분류: 분석된 데이터 검색
            응대품질 분석 전용 분석기(korean_analysis_index_analyzer)로 인덱싱된
            allText.analysis 서브필드를 검색합니다.

            이 API가 사용하는 사전: analysis_synonyms.txt (분석 전용)
            검색 사전(synonyms.txt)은 적용되지 않습니다.

            활용 예시
            - keyword=미납       → 미납 관련 상담 패턴 탐지
            - keyword=해지       → 해지 의도 상담 군집 분석
            - keyword=친절응대   → 응대품질 우수 상담 검색
            - keyword=불만감정   → 고객 불만 감정 표현 상담 탐지

            참조 DB: Elasticsearch consult-index (allText.analysis 서브필드)
                     + MySQL consultation_raw_texts (rawTextJson 병합)
            """
    )
    @GetMapping("/analysis/keywords")
    public ResponseEntity<List<ConsultAnalysisResponse>> analyzeKeywords(
            @Parameter(description = "분석할 키워드 (analysis_synonyms.txt 동의어 적용)", example = "미납")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        List<ConsultDoc> docs = consultSearchService.searchByAnalysisKeyword(keyword, page, size);
        return ResponseEntity.ok(enrichWithRawText(docs));
    }

    /**
     * ES 결과 목록의 consultId로 MySQL raw_text_json을 배치 조회하여 병합.
     * N+1 없이 IN 쿼리 한 번으로 처리.
     */
    private List<ConsultAnalysisResponse> enrichWithRawText(List<ConsultDoc> docs) {
        if (docs.isEmpty()) return List.of();

        List<Long> consultIds = docs.stream()
                .map(ConsultDoc::getConsultId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, String> rawTextMap = rawTextRepository.findByConsultIdIn(consultIds).stream()
                .collect(Collectors.toMap(
                        ConsultationRawText::getConsultId,
                        ConsultationRawText::getRawTextJson,
                        (a, b) -> a
                ));

        return docs.stream()
                .map(doc -> ConsultAnalysisResponse.of(doc, rawTextMap.get(doc.getConsultId())))
                .toList();
    }
}
