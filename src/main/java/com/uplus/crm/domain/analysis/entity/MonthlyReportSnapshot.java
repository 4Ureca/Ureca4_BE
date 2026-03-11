package com.uplus.crm.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

// 월별 팀 평균
@Getter
@NoArgsConstructor
@Document(collection = "monthly_report_snapshot")
public class MonthlyReportSnapshot extends BaseTotalSnapshot {
}
