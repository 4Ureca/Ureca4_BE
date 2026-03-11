package com.uplus.crm.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

// 주별 상담사 개인 스냅샷
@Getter
@NoArgsConstructor
@Document(collection = "weekly_agent_report_snapshot")
public class WeeklyAgentReportSnapshot extends BaseAgentSnapshot {
}
