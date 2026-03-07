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
@Tag(name = "Dictionary Admin", description = "ES 검색 사전 추출·업데이트 (관리자 전용)")
@RestController
@RequestMapping("/admin/dictionary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DictionaryUpdateController {

    private final DictionaryUpdateService dictionaryUpdateService;

    @Operation(
            summary = "사전 전체 업데이트 실행",
            description = """
                    MongoDB에서 키워드를 추출하고, 사전 파일에 기록한 뒤 ES 분석기를 리로드합니다.

                    **실행 순서**
                    1. `summary.keywords` + `iam.matchKeyword` 빈도 집계
                    2. 신규 어휘를 `dictionary.output-path` 파일에 추가 기록
                    3. `POST /consult-index/_reload_search_analyzers` 호출

                    **사전 파일 반영 조건**
                    `application.yml` 에 `dictionary.output-path` 가 설정되어 있고,
                    해당 경로가 ES 컨테이너의 `config/analysis/userdict.txt` 와 동일한
                    볼륨으로 마운트되어 있어야 합니다.
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
            summary = "키워드 추출 (미리보기)",
            description = """
                    MongoDB에서 키워드를 추출하여 반환합니다. 파일 기록·분석기 리로드는 하지 않습니다.
                    사전 업데이트 전 추출 결과를 확인할 때 사용합니다.

                    **데이터 소스**
                    - `summary.keywords`: AI 추출 상담 키워드 (주 소스)
                    - `iam.matchKeyword`: IAM 매칭 키워드 (보조 소스)
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
            summary = "ES 분석기 리로드 (파일 업데이트 후 사용)",
            description = """
                    사전 파일을 수동으로 편집한 후 ES 분석기를 리로드합니다.
                    `POST /consult-index/_reload_search_analyzers` 를 직접 호출하는 것과 동일합니다.
                    """)
    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reload() {
        dictionaryUpdateService.reloadAnalyzers();
        return ResponseEntity.ok(Map.of("message", "ES 분석기 리로드 완료"));
    }
}
