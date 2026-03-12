package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.service.DictionaryUpdateService;
import com.uplus.crm.domain.elasticsearch.service.DictionaryUpdateService.KeywordEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 사전(Custom Dictionary) 관리 API.
 * 관리자 전용 (ROLE_ADMIN).
 */
@Tag(name = "② ES 사전 관리")
@RestController
@RequestMapping("/admin/dictionary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DictionaryUpdateController {

    private final DictionaryUpdateService dictionaryUpdateService;

    @Operation(

            summary = "MongoDB 키워드 자동 추출 → userdict.txt 추가",
            description = """
                    MongoDB `consultation_summary`에서 자주 등장하는 키워드를 추출하여
                    `userdict.txt`에 자동으로 추가하고 ES 분석기를 리로드합니다.
                    상담 데이터가 쌓인 후 주기적으로 실행하면 검색 품질이 향상됩니다.

                    **실행 순서**
                    1. `summary.keywords` + `iam.matchKeyword` 빈도 집계 (최대 500건)
                    2. 신규 키워드를 `userdict.txt`에 추가 기록
                    3. ES `_reload_search_analyzers` 호출

                    `dictionary.output-path` 미설정 시 파일 기록 없이 리로드만 실행됩니다.
                    `analysis_synonyms.txt` 변경 후에는 이 API가 아닌 인덱스 재생성이 필요합니다.
                    """)
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update() {
        List<KeywordEntry> keywords = dictionaryUpdateService.runUpdate();
        return ResponseEntity.ok(Map.of(
                "extractedCount", keywords.size(),
                "topKeywords", keywords.stream().limit(20).toList(),
                "message", "사전 업데이트 및 분석기 리로드 완료"
        ));
    }

    @Operation(

            summary = "추출될 키워드 미리보기 (파일 변경 없음)",
            description = """
                    MongoDB에서 키워드를 추출하여 미리보기만 반환합니다.
                    파일 기록·분석기 리로드는 하지 않습니다.
                    `/update` 실행 전 어떤 키워드가 추가될지 확인할 때 사용하세요.
                    """)
    @GetMapping("/extract")
    public ResponseEntity<Map<String, Object>> extract(
            @Parameter(description = "반환할 최대 키워드 수 (기본 100)", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        List<KeywordEntry> keywords = dictionaryUpdateService.extractKeywords(limit);
        return ResponseEntity.ok(Map.of(
                "total", keywords.size(),
                "keywords", keywords
        ));
    }

    @Operation(

            summary = "ES 분석기만 리로드 (search-time 필터 한정)",
            description = """
                    ES `_reload_search_analyzers`를 호출합니다.
                    `synonym_graph` 검색 분석기만 갱신됩니다.

                    `analysis_synonyms.txt` 변경 후에는 index-time 필터도 바뀌므로
                    이 API만으로는 부족합니다. `POST /es-test/recreate-index` 를 사용하세요.
                    """)
    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reload() {
        dictionaryUpdateService.reloadAnalyzers();
        return ResponseEntity.ok(Map.of("message", "ES 분석기 리로드 완료"));
    }
}
