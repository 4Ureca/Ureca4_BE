package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.service.ConsultIndexSyncService;
import com.uplus.crm.domain.elasticsearch.service.ConsultIndexSyncService.SyncResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "① ES 셋업")
@RestController
@RequestMapping("/admin/es")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConsultIndexSyncController {

    private final ConsultIndexSyncService syncService;

    @Operation(
            summary = "[셋업 Step 2] 로컬 DB 전체 데이터 ES 동기화",
            description = """
                    Batch 프로젝트로 생성된 로컬 MySQL + MongoDB 데이터를 ES에 동기화합니다.
                    최초 세팅 시 또는 사전 변경 후 인덱스 재생성 시에 호출합니다.

                    [로직 순서]
                    1. MySQL consultation_results 전체 조회 (100건 단위 페이징)
                    2. MySQL consultation_raw_texts 조회 → 전체 대화 평문 추출 (rawText)
                    3. 상담사 발화만 별도 추출 → hasGreeting / hasFarewell 자동 감지
                    4. MongoDB consultation_summary 조회 → sentiment, riskFlags, 고객정보 보완
                    5. ES consult-index에 upsert (consultId = 문서 ID, 재호출 시 중복 없음)

                    [참조 DB]
                    읽기: MySQL consultation_results (iamIssue, iamAction, iamMemo, createdAt)
                    읽기: MySQL consultation_raw_texts (raw_text_json)
                    읽기: MongoDB consultation_summary (sentiment, riskFlags, customer, keywords)
                    쓰기: Elasticsearch consult-index

                    [skip 조건]
                    consultation_raw_texts 가 없는 건은 자동으로 건너뜁니다.
                    """)
    @PostMapping("/sync")
    public ResponseEntity<SyncResult> syncAll() {
        SyncResult result = syncService.syncAll();
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "단일 상담 ES 재동기화",
            description = """
                    특정 consultId 1건만 ES에 재동기화합니다.
                    특정 상담 데이터만 수정되었거나 동기화 오류가 발생한 경우 사용합니다.

                    [참조 DB]
                    읽기: MySQL consultation_results, consultation_raw_texts
                    읽기: MongoDB consultation_summary
                    쓰기: Elasticsearch consult-index (해당 consultId 문서만 갱신)
                    """)
    @PostMapping("/sync/{consultId}")
    public ResponseEntity<SyncResult> syncOne(
            @Parameter(description = "동기화할 상담 ID (MySQL consultation_results.consult_id)", example = "1001")
            @PathVariable Long consultId) {
        SyncResult result = syncService.syncOne(consultId);
        return ResponseEntity.ok(result);
    }
}
