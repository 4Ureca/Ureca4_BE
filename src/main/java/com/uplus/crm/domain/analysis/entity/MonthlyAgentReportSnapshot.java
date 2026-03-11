package com.uplus.crm.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

// 월별 상담사 개인 스냅샷
@Getter
@NoArgsConstructor
@Document(collection = "monthly_agent_report_snapshot")
public class MonthlyAgentReportSnapshot extends BaseAgentSnapshot {
}