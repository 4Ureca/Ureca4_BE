package com.uplus.crm.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

// 주별 팀 평균
@Getter
@NoArgsConstructor
@Document(collection = "weekly_report_snapshot")
public class WeeklyReportSnapshot extends BaseTotalSnapshot {
}
