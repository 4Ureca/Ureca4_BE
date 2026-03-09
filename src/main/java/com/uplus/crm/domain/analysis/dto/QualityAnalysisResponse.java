package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 응대 품질 분석 응답 DTO (일별/주별/월별 공용)
 *
 * daily/weekly/monthly_agent_report_snapshot.qualityAnalysis에서 추출.
 *
 * <h3>품질 분석 기준</h3>
 * <p>상담사의 응대 원문을 Elasticsearch 형태소 분석기(nori + 동의어 사전)로 토큰화한 뒤,
 * 아래 7개 분류에 해당하는 토큰이 포함되었는지를 기반으로 측정합니다.</p>
 * <ul>
 *   <li><b>공감 표현</b> — "그러셨군요", "많이 힘드셨겠다", "불편하셨겠어요" 등 → 건당 등장 횟수 합산</li>
 *   <li><b>사과 표현</b> — "죄송합니다", "불편을 드려 죄송합니다" 등 → 포함 여부 (비율)</li>
 *   <li><b>마무리 인사</b> — "수고하세요", "좋은 하루 되세요", "감사합니다" 등 → 포함 여부</li>
 *   <li><b>친절 표현</b> — "도움이 되셨으면 좋겠습니다", "편하게 말씀해 주세요" 등 → 포함 여부</li>
 *   <li><b>신속 응대</b> — "바로 확인해 드리겠습니다", "즉시 처리" 등 → 포함 여부</li>
 *   <li><b>정확 응대</b> — "정확히 안내 드리겠습니다", "확인 결과" 등 → 포함 여부</li>
 *   <li><b>대기 안내</b> — "잠시만요", "확인해 드리겠습니다", "잠시 기다려 주세요" 등 → 포함 여부</li>
 * </ul>
 *
 * <h3>종합 점수 산출</h3>
 * <p>7개 지표에 가중치를 적용하여 5점 만점으로 환산합니다.</p>
 * <pre>
 * totalScore = (공감 0.20 + 사과 0.10 + 마무리 0.10 + 친절 0.15
 *              + 신속 0.15 + 정확 0.15 + 대기안내 0.15) × 5.0
 * </pre>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "응대 품질 분석")
public class QualityAnalysisResponse {

    @Schema(description = "상담사 ID", example = "1")
    private Long agentId;

    @Schema(description = "집계 시작일", example = "2025-01-13")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-19")
    private String endDate;

    @Schema(description = "상담 처리 건수", example = "15")
    private int consultCount;

    @Schema(description = "품질 분석 완료 건수 (원문이 존재하여 실제 분석된 상담 수)", example = "12")
    private int analyzedCount;

    @Schema(description = "공감 표현 총 횟수 — '그러셨군요', '힘드셨겠다' 등 공감 토큰의 전체 등장 횟수 합산", example = "5")
    private long empathyCount;

    @Schema(description = "건당 평균 공감 횟수 (empathyCount ÷ analyzedCount)", example = "0.42")
    private double avgEmpathyPerConsult;

    @Schema(description = "사과 표현 포함 비율 (%) — '죄송합니다', '불편을 드려' 등이 포함된 상담 비율", example = "33.3")
    private double apologyRate;

    @Schema(description = "마무리 인사 포함 비율 (%) — '수고하세요', '좋은 하루 되세요', '감사합니다' 등", example = "60.0")
    private double closingRate;

    @Schema(description = "친절 표현 포함 비율 (%) — '편하게 말씀해 주세요', '도움이 되셨으면' 등", example = "20.0")
    private double courtesyRate;

    @Schema(description = "신속 응대 포함 비율 (%) — '바로 확인', '즉시 처리' 등 신속 대응 표현", example = "13.3")
    private double promptnessRate;

    @Schema(description = "정확 응대 포함 비율 (%) — '정확히 안내', '확인 결과' 등 정확성 관련 표현", example = "6.7")
    private double accuracyRate;

    @Schema(description = "대기 안내 포함 비율 (%) — '잠시만요', '확인해 드리겠습니다' 등 대기 안내 표현", example = "40.0")
    private double waitingGuideRate;

    @Schema(description = "종합 점수 (0~5) — 7개 지표 가중 합산 × 5.0", example = "2.1")
    private double totalScore;
}
