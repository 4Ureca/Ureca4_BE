package com.uplus.crm.domain.analysis.service.agent;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
import com.uplus.crm.domain.analysis.dto.agent.AgentSatisfactionResponse;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.analysis.entity.BaseAgentSnapshot;
import com.uplus.crm.domain.analysis.entity.BaseTotalSnapshot;
import com.uplus.crm.domain.analysis.repository.DailyAgentReportRepository;
import com.uplus.crm.domain.analysis.repository.DailyReportRepository;
import com.uplus.crm.domain.analysis.repository.MonthlyAgentReportRepository;
import com.uplus.crm.domain.analysis.repository.MonthlyReportRepository;
import com.uplus.crm.domain.analysis.repository.WeeklyAgentReportRepository;
import com.uplus.crm.domain.analysis.repository.WeeklyReportRepository;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentReportService {

  private final DailyAgentReportRepository dailyAgentRepo;
  private final WeeklyAgentReportRepository weeklyAgentRepo;
  private final MonthlyAgentReportRepository monthlyAgentRepo;

  private final DailyReportRepository dailyTotalRepo;
  private final WeeklyReportRepository weeklyTotalRepo;
  private final MonthlyReportRepository monthlyTotalRepo;

  private final EmployeeRepository employeeRepository;


  // 1. 전체 성과 (Metrics) 조회
  public AgentMetricsResponse getMetrics(String period, Integer empId, LocalDate date) {
    //  1. 상담사 존재 여부 체크 (MySQL 조회)
    if (!employeeRepository.existsById(empId)) {
      throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
    }
    LocalDateTime adjustedDate = getAdjustedDate(period, date.atStartOfDay());

    // 보정된 날짜를 두 메서드에 똑같이 넘겨줍니다.
    BaseAgentSnapshot mySnap = getAgentSnapshot(period, empId, adjustedDate);
    BaseTotalSnapshot teamSnap = getTotalSnapshot(period, adjustedDate);

    // 2. 스냅샷 데이터 존재 여부 체크 (MongoDB 조회)
    if (mySnap == null) {
      throw new BusinessException(ErrorCode.REPORT_DATA_NOT_FOUND);
    }


    return AgentMetricsResponse.builder()
        .empId(empId.toString())
        .startedAt(mySnap != null ? mySnap.getStartAt().toLocalDate() : adjustedDate.toLocalDate())
        .endedAt(mySnap != null ? mySnap.getEndAt().toLocalDate() : null)
        .myConsultCount(mySnap != null ? mySnap.getConsultCount() : 0)
        .teamAvgConsultCount(teamSnap != null && teamSnap.getAvgConsultPerAgent() != null ? teamSnap.getAvgConsultPerAgent() : 0.0)
        .myAvgDuration(mySnap != null ? formatDuration(mySnap.getAvgDurationMinutes()) : "0:00")
        .teamAvgDuration(teamSnap != null ? formatDuration(teamSnap.getAvgDurationMinutes()) : "0:00")
        .myQualityScore(mySnap != null && mySnap.getQualityAnalysis() != null && mySnap.getQualityAnalysis().getTotalScore() != null ? mySnap.getQualityAnalysis().getTotalScore() : 0.0)
        .teamAvgQualityScore(teamSnap != null && teamSnap.getAvgQualityScore() != null ? teamSnap.getAvgQualityScore() : 0.0)
        .mySatisfactionScore(mySnap != null && mySnap.getCustomerSatisfactionAnalysis() != null ? mySnap.getCustomerSatisfactionAnalysis().getSatisfactionScore() : 0.0)
        .teamAvgSatisfactionScore(teamSnap != null && teamSnap.getAvgSatisfiedScore() != null ? teamSnap.getAvgSatisfiedScore() : 0.0)
        .iamMatchRate(mySnap != null && mySnap.getIamKeywordMatchAnalysis() != null
            ? mySnap.getIamKeywordMatchAnalysis() : 0.0)
        .build();
  }

  // 2. 처리 카테고리 (Categories) 조회
  public List<CategoryRankingDto> getCategories(String period, Integer empId, LocalDate date) {

    //  1. 상담사 존재 여부 체크 (MySQL 조회)
    if (!employeeRepository.existsById(empId)) {
      throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
    }

    LocalDateTime adjustedDate = getAdjustedDate(period, date.atStartOfDay());
    BaseAgentSnapshot mySnap = getAgentSnapshot(period, empId, adjustedDate);

//    if (mySnap == null || mySnap.getCategoryRanking() == null) return Collections.emptyList();
    // 2. 스냅샷 데이터 존재 여부 체크 (MongoDB 조회)
    if (mySnap == null) {
      throw new BusinessException(ErrorCode.REPORT_DATA_NOT_FOUND);
    }

    return groupCategories(mySnap.getCategoryRanking(), empId.toString(), mySnap.getStartAt(), mySnap.getEndAt());
  }

  // 3. 고객 만족도 (Satisfaction) 조회
  public AgentSatisfactionResponse getSatisfaction(String period, Integer empId, LocalDate date) {

    //  1. 상담사 존재 여부 체크 (MySQL 조회)
    if (!employeeRepository.existsById(empId)) {
      throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
    }

    LocalDateTime adjustedDate = getAdjustedDate(period, date.atStartOfDay());
    BaseAgentSnapshot mySnap = getAgentSnapshot(period, empId, adjustedDate);
    BaseTotalSnapshot teamSnap = getTotalSnapshot(period, adjustedDate);

    var mySat = (mySnap != null) ? mySnap.getCustomerSatisfactionAnalysis() : null;
    // 2. 스냅샷 데이터 존재 여부 체크 (MongoDB 조회)
    if (mySnap == null) {
      throw new BusinessException(ErrorCode.REPORT_DATA_NOT_FOUND);
    }

    return AgentSatisfactionResponse.builder()
        .empId(empId.toString())
        .startedAt(mySnap != null ? mySnap.getStartAt().toLocalDate() : adjustedDate.toLocalDate())
        .endedAt(mySnap != null ? mySnap.getEndAt().toLocalDate() : null)
        .satisfactionScore(mySat != null ? mySat.getSatisfactionScore() : 0.0)
        .teamAvgSatisfactionScore(teamSnap != null && teamSnap.getAvgSatisfiedScore() != null ? teamSnap.getAvgSatisfiedScore() : 0.0)
        .responseRate(mySat != null ? mySat.getResponseRate() : 0.0)
        .build();
  }

  // --- Helper Methods ---

  // 반환 타입을 BaseAgentSnapshot으로
  private BaseAgentSnapshot getAgentSnapshot(String period, Integer empId, LocalDateTime startAt) {

    return switch (period.toLowerCase()) {
      case "daily" -> dailyAgentRepo.findByAgentIdAndStartAt(empId.longValue(), startAt).orElse(null);
      case "weekly" -> weeklyAgentRepo.findByAgentIdAndStartAt(empId.longValue(), startAt).orElse(null);
      case "monthly" -> monthlyAgentRepo.findByAgentIdAndStartAt(empId.longValue(), startAt).orElse(null);
      default -> null;
    };
  }

  private BaseTotalSnapshot getTotalSnapshot(String period, LocalDateTime startAt) {
    return switch (period.toLowerCase()) {
      case "daily" -> dailyTotalRepo.findByStartAt(startAt).orElse(null);
      case "weekly" -> weeklyTotalRepo.findByStartAt(startAt).orElse(null);
      case "monthly" -> monthlyTotalRepo.findByStartAt(startAt).orElse(null);
      default -> null;
    };
  }

  private String formatDuration(Double minutes) {
    if (minutes == null || minutes == 0) return "0:00";
    int m = minutes.intValue();
    int s = (int) Math.round((minutes - m) * 60);
    return String.format("%d:%02d", m, s);
  }

  // 파라미터 타입을 BaseAgentSnapshot 내부 클래스로 변경
  private List<CategoryRankingDto> groupCategories(List<BaseAgentSnapshot.CategoryRanking> rankings,
      String empId, LocalDateTime start, LocalDateTime end) {
    if (rankings == null || rankings.isEmpty()) return Collections.emptyList();

    return rankings.stream()
        .collect(Collectors.groupingBy(BaseAgentSnapshot.CategoryRanking::getLarge))
        .entrySet().stream()
        .map(entry -> CategoryRankingDto.builder()
            .empId(empId)
            .startedAt(start.toLocalDate())
            .endedAt(end != null ? end.toLocalDate() : null)
            .name(entry.getKey())
            .totalCount(entry.getValue().stream().mapToInt(BaseAgentSnapshot.CategoryRanking::getCount).sum())
            .mediumCategories(entry.getValue().stream()
                .map(m -> CategoryRankingDto.MediumCategoryDto.builder()
                    .name(m.getMedium())
                    .count(m.getCount())
                    .build())
                .collect(Collectors.toList()))
            .build())
        .sorted((a, b) -> b.getTotalCount().compareTo(a.getTotalCount()))
        .collect(Collectors.toList());
  }

  // 날짜 보정
  private LocalDateTime getAdjustedDate(String period, LocalDateTime startAt) {
    if ("weekly".equals(period)) {
      return startAt.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          .truncatedTo(ChronoUnit.DAYS);
    } else if ("monthly".equals(period)) {
      return startAt.with(TemporalAdjusters.firstDayOfMonth())
          .truncatedTo(ChronoUnit.DAYS);
    }
    return startAt; // daily는 그대로 반환
  }
}