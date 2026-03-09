package com.uplus.crm.domain.elasticsearch.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "consult-index", createIndex = false)
public class ConsultDoc {

    @Id
    private String id;

    /** MongoDB consultation_summary.consultId 와 매핑 — ES→MongoDB 조인 키 */
    @Field(type = FieldType.Long)
    private Long consultId;

    @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer")
    private String content;

    @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer")
    private String iamIssue;

    @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer")
    private String iamAction;

    @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer")
    private String iamMemo;

    /**
     * 검색용: korean_index_analyzer (기본)<br>
     * 분석용: allText.analysis 서브필드 → korean_analysis_index_analyzer
     * (인삿말·응대 어근 제거, discard 모드 토크나이저)
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text,
                    analyzer = "korean_index_analyzer",
                    searchAnalyzer = "korean_search_analyzer"),
            otherFields = {
                    @InnerField(suffix = "analysis", type = FieldType.Text,
                            analyzer = "korean_analysis_index_analyzer",
                            searchAnalyzer = "korean_analysis_search_analyzer")
            }
    )
    private String allText;

    @Field(type = FieldType.Keyword)
    private String sentiment;

    @Field(type = FieldType.Integer)
    private Integer riskScore;

    @Field(type = FieldType.Keyword)
    private String priority;

    @Field(type = FieldType.Keyword)
    private String customerId;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /** 인사말 포함 여부 (응대품질 분석용) — 저장 시 자동 감지 */
    @Field(type = FieldType.Boolean)
    private Boolean hasGreeting;

    /** 마무리 인사 포함 여부 (응대품질 분석용) — 저장 시 자동 감지 */
    @Field(type = FieldType.Boolean)
    private Boolean hasFarewell;

    /**
     * 상담 원문 텍스트 — consultation_raw_texts.raw_text_json 에서 추출한 평문.
     * 인덱싱 시 {@code ConsultSearchService.extractRawText(rawTextJson)} 으로 변환하여 설정.
     * keyword 검색 대상 필드에 포함됨 (가중치 1.0).
     */
    @Field(type = FieldType.Text, analyzer = "korean_index_analyzer", searchAnalyzer = "korean_search_analyzer")
    private String rawText;
}