package com.uplus.crm.domain.analysis.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// 일별 팀 평균
@Getter
@NoArgsConstructor
@Document(collection = "daily_report_snapshot")
public class DailyReportSnapshot extends BaseTotalSnapshot {
}
