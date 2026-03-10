package com.uplus.crm.domain.analysis.entity;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;


@Document(collection = "daily_agent_report_snapshot")
public class DailyAgentReportSnapshot extends BaseAgentSnapshot {
}