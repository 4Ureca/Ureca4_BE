package com.uplus.crm.domain.elasticsearch.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "consult-index")
@Setting(settingPath = "elasticsearch/consult-settings.json")
public class ConsultDoc {

    @Id
    private String id;

    // 모든 분석기 이름을 korean_analyzer로 통일합니다.
    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String content;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String iamIssue;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String iamAction;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String iamMemo;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
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
            mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}