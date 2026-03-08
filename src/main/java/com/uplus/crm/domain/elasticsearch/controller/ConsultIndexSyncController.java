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
@Tag(name = "ES Index Sync", description = "대화원문 → ES 동기화 (관리자 전용)")
@RestController
@RequestMapping("/admin/es")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConsultIndexSyncController {

    private final ConsultIndexSyncService syncService;

    @Operation(
            summary = "전체 상담 데이터 ES 동기화",
            description = """
                    MySQL `consultation_raw_texts`의 실제 대화원문을 읽어 ES `consult-index`에 동기화합니다.

                    **파이프라인**
                    1. MySQL `consultation_results` 전체 조회 (100건 단위 페이징)
                    2. MySQL `consultation_raw_texts` → 전체 대화 평문 추출 (검색용 `rawText`)
                    3. 상담사 발화만 별도 추출 → `hasGreeting` / `hasFarewell` 정확 감지
                    4. MongoDB `consultation_summary` → AI 요약·감정·위험도 보완
                    5. ES `consult-index` upsert (consultId 기준 중복 방지)

                    **skip 조건**: `consultation_raw_texts`가 없는 상담 건은 건너뜁니다.

                    **동기화 후 확인**
                    - `GET /elasticsearch/consult/analysis/quality?hasGreeting=false`
                      → 실제 인사말 없이 시작한 상담 조회
                    """)
    @PostMapping("/sync")
    public ResponseEntity<SyncResult> syncAll() {
        SyncResult result = syncService.syncAll();
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "단일 상담 ES 동기화",
            description = """
                    지정한 consultId 한 건을 ES에 동기화합니다.
                    대화원문 수정 후 특정 건만 재동기화할 때 사용합니다.
                    """)
    @PostMapping("/sync/{consultId}")
    public ResponseEntity<SyncResult> syncOne(
            @Parameter(description = "동기화할 상담 ID", example = "1001")
            @PathVariable Long consultId) {
        SyncResult result = syncService.syncOne(consultId);
        return ResponseEntity.ok(result);
    }
}
