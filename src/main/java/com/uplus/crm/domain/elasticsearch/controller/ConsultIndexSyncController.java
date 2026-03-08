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

/**
 * MySQL(consultation_raw_texts) → ES(consult-index) 동기화 API.
 *
 * <p>동기화 후 {@code /elasticsearch/consult/analysis/quality} 에서
 * 실제 대화원문 기반 응대품질 분석 결과를 확인할 수 있다.</p>
 */
@Tag(name = "① ES 셋업")
@RestController
@RequestMapping("/admin/es")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConsultIndexSyncController {

    private final ConsultIndexSyncService syncService;

    @Operation(
            tags = {"① ES 셋업"},
            summary = "[셋업 Step 2A] 실제 대화원문 ES 동기화 — 운영 데이터 사용 시",
            description = """
                    MySQL `consultation_raw_texts`의 **실제 상담 대화원문**을 ES에 동기화합니다.
                    더미 데이터가 아닌 실제 운영 데이터로 분석하려면 이 API를 사용하세요.

                    **파이프라인**
                    1. MySQL `consultation_results` 전체 (100건 단위 페이징)
                    2. MySQL `consultation_raw_texts` → 전체 대화 평문 (검색용 rawText)
                    3. 상담사 발화만 추출 → hasGreeting / hasFarewell 정확 감지
                    4. MongoDB `consultation_summary` → 감정·위험도·고객정보 보완
                    5. ES upsert (consultId = 문서 ID → 재호출 시 중복 없음)

                    consultation_raw_texts 가 없는 건은 자동으로 건너뜁니다.
                    """)
    @PostMapping("/sync")
    public ResponseEntity<SyncResult> syncAll() {
        SyncResult result = syncService.syncAll();
        return ResponseEntity.ok(result);
    }

    @Operation(
            tags = {"① ES 셋업"},
            summary = "단일 상담 ES 재동기화",
            description = """
                    특정 consultId 1건만 ES에 재동기화합니다.
                    전체 동기화 없이 특정 건의 데이터만 갱신할 때 사용합니다.
                    """)
    @PostMapping("/sync/{consultId}")
    public ResponseEntity<SyncResult> syncOne(
            @Parameter(description = "동기화할 상담 ID", example = "1001")
            @PathVariable Long consultId) {
        SyncResult result = syncService.syncOne(consultId);
        return ResponseEntity.ok(result);
    }
}
