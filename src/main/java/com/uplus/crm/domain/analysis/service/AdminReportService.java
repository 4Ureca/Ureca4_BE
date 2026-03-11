package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.CategorySummaryResponse;
import com.uplus.crm.domain.analysis.dto.ChurnDefenseResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.KeywordRankingResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.SubscriptionAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.TimeSlotTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 관리자 리포트 통합 서비스
 *
 * period(daily/weekly/monthly)에 따라 적절한 서비스로 위임합니다.
 */
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final DailyReportService dailyReportService;
    private final PerformanceReportService performanceReportService;
    private final MonthlyReportService monthlyReportService;

    // ==================== 성과 요약 ====================

    public Optional<PerformanceSummaryResponse> getPerformanceSummary(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "daily" -> dailyReportService.getDailyPerformanceSummary(date);
            case "weekly" -> performanceReportService.getWeeklyPerformanceSummary(date);
            case "monthly" -> performanceReportService.getMonthlyPerformanceSummary(date);
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 상담사 순위 ====================

    public Optional<AgentRankingResponse> getAgentRanking(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "daily" -> dailyReportService.getDailyAgentRanking(date);
            case "weekly" -> performanceReportService.getWeeklyAgentRanking(date);
            case "monthly" -> performanceReportService.getMonthlyAgentRanking(date);
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 고객 특이사항 ====================

    public CustomerRiskResponse getCustomerRisk(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "daily" -> dailyReportService.getCustomerRisk(date);
            case "monthly" -> monthlyReportService.getMonthlyCustomerRisk(date);
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    public CustomerRiskCompareResponse compareCustomerRisk(String period, LocalDate baseDate, LocalDate compareDate) {
        if (!"daily".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        return dailyReportService.compareCustomerRisk(baseDate, compareDate);
    }

    // ==================== 시간대별 트렌드 (daily only) ====================

    public Optional<TimeSlotTrendResponse> getTimeSlotTrend(String period, LocalDate date, String slot) {
        if (!"daily".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        return dailyReportService.getTimeSlotTrend(date, slot);
    }

    // ==================== 카테고리 요약 (daily only) ====================

    public Optional<CategorySummaryResponse> getCategorySummary(String period, LocalDate date, String slot) {
        if (!"daily".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        return dailyReportService.getCategorySummary(date, slot);
    }

    // ==================== 키워드 TOP ====================

    /**
     * daily: KeywordRankingResponse 반환 (slot 지원)
     * weekly/monthly: KeywordAnalysisResponse 반환 (topKeywords만)
     */
    public Object getKeywordTop(String period, LocalDate date, String slot) {
        return switch (period.toLowerCase()) {
            case "daily" -> dailyReportService.getKeywordRanking(date, slot).orElse(null);
            case "weekly" -> performanceReportService.getWeeklyKeywordAnalysis(date)
                    .map(r -> KeywordAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .topKeywords(r.getTopKeywords())
                            .build())
                    .orElse(null);
            case "monthly" -> {
                KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(date);
                yield full != null ? KeywordAnalysisResponse.builder()
                        .startDate(full.getStartDate())
                        .endDate(full.getEndDate())
                        .topKeywords(full.getTopKeywords())
                        .build() : null;
            }
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 키워드 장기 유지 (weekly, monthly) ====================

    public Object getKeywordLongTerm(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "weekly" -> performanceReportService.getWeeklyKeywordAnalysis(date)
                    .map(r -> KeywordAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .longTermTopKeywords(r.getLongTermTopKeywords())
                            .build())
                    .orElse(null);
            case "monthly" -> {
                KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(date);
                yield full != null ? KeywordAnalysisResponse.builder()
                        .startDate(full.getStartDate())
                        .endDate(full.getEndDate())
                        .longTermTopKeywords(full.getLongTermTopKeywords())
                        .build() : null;
            }
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 키워드 고객 유형별 ====================

    public Object getKeywordCustomerTypes(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "daily" -> dailyReportService.getDailyCustomerTypeKeywords(date).orElse(null);
            case "weekly" -> performanceReportService.getWeeklyKeywordAnalysis(date)
                    .map(r -> KeywordAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .byCustomerType(r.getByCustomerType())
                            .build())
                    .orElse(null);
            case "monthly" -> {
                KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(date);
                yield full != null ? KeywordAnalysisResponse.builder()
                        .startDate(full.getStartDate())
                        .endDate(full.getEndDate())
                        .byCustomerType(full.getByCustomerType())
                        .build() : null;
            }
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 구독상품 (weekly, monthly) ====================

    public Optional<SubscriptionAnalysisResponse> getSubscriptionProducts(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "weekly" -> performanceReportService.getWeeklySubscription(date)
                    .map(r -> SubscriptionAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .newSubscriptions(r.getNewSubscriptions())
                            .canceledSubscriptions(r.getCanceledSubscriptions())
                            .build());
            case "monthly" -> performanceReportService.getMonthlySubscription(date)
                    .map(r -> SubscriptionAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .newSubscriptions(r.getNewSubscriptions())
                            .canceledSubscriptions(r.getCanceledSubscriptions())
                            .build());
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    public Optional<SubscriptionAnalysisResponse> getSubscriptionAgeGroups(String period, LocalDate date) {
        return switch (period.toLowerCase()) {
            case "weekly" -> performanceReportService.getWeeklySubscription(date)
                    .map(r -> SubscriptionAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .byAgeGroup(r.getByAgeGroup())
                            .build());
            case "monthly" -> performanceReportService.getMonthlySubscription(date)
                    .map(r -> SubscriptionAnalysisResponse.builder()
                            .startDate(r.getStartDate())
                            .endDate(r.getEndDate())
                            .byAgeGroup(r.getByAgeGroup())
                            .build());
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 해지방어 (monthly only) ====================

    public ChurnDefenseResponse getChurnDefenseSummary(String period, LocalDate date) {
        if (!"monthly".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(date);
        if (full == null) return null;
        return ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .totalAttempts(full.getTotalAttempts())
                .successCount(full.getSuccessCount())
                .successRate(full.getSuccessRate())
                .avgDurationSec(full.getAvgDurationSec())
                .build();
    }

    public ChurnDefenseResponse getChurnDefenseComplaintReasons(String period, LocalDate date) {
        if (!"monthly".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(date);
        if (full == null) return null;
        return ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .complaintReasons(full.getComplaintReasons())
                .build();
    }

    public ChurnDefenseResponse getChurnDefenseCustomerTypes(String period, LocalDate date) {
        if (!"monthly".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(date);
        if (full == null) return null;
        return ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .byCustomerType(full.getByCustomerType())
                .build();
    }

    public ChurnDefenseResponse getChurnDefenseActions(String period, LocalDate date) {
        if (!"monthly".equalsIgnoreCase(period)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(date);
        if (full == null) return null;
        return ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .byAction(full.getByAction())
                .build();
    }
}
