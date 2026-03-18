package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.analysis.entity.BaseAgentSnapshot;
import com.uplus.crm.domain.analysis.entity.DailyAgentReportSnapshot;
import com.uplus.crm.domain.analysis.entity.MonthlyAgentReportSnapshot;
import com.uplus.crm.domain.analysis.repository.*;
import com.uplus.crm.domain.analysis.service.agent.AgentReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AgentReportServiceTest {

  @Mock private DailyAgentReportRepository dailyAgentRepo;
  @Mock private WeeklyAgentReportRepository weeklyAgentRepo;
  @Mock private MonthlyAgentReportRepository monthlyAgentRepo;
  @Mock private DailyReportRepository dailyTotalRepo;
  @Mock private WeeklyReportRepository weeklyTotalRepo;
  @Mock private MonthlyReportRepository monthlyTotalRepo;
  @Mock private EmployeeRepository employeeRepository;

  @InjectMocks
  private AgentReportService agentReportService;

  private final Integer EMP_ID = 101;
  private final LocalDate DATE = LocalDate.of(2026, 3, 16);

  // --- [에러 케이스 테스트] ---

  @Test
  @DisplayName("상담사 존재하지 않을 때 204")
  void getMetrics_AgentNotFound() {
    // given
    given(employeeRepository.existsById(EMP_ID)).willReturn(false);

    // when
    AgentMetricsResponse result = agentReportService.getMetrics("daily", EMP_ID, DATE);

    // then (이제 예외가 발생하지 않고 null이 나와야 함)
    assertThat(result).isNull();

//    // when & then
//    assertThatThrownBy(() -> agentReportService.getMetrics("daily", EMP_ID, DATE))
//        .isInstanceOf(BusinessException.class)
//        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AGENT_NOT_FOUND);
  }

  @Test
  @DisplayName("몽고DB에 리포트 스냅샷 데이터가 없을 때 REPORT_DATA_NOT_FOUND 예외 발생")
  void getMetrics_ReportDataNotFound() {
//    // given
//    given(employeeRepository.existsById(EMP_ID)).willReturn(true);
//    LocalDateTime targetTime = DATE.atStartOfDay();
//
//    // 내 스냅샷이 없는 경우 (Optional.empty 반환)
//    given(dailyAgentRepo.findByAgentIdAndStartAt(EMP_ID.longValue(), targetTime))
//        .willReturn(Optional.empty());
//
//    // when & then
//    assertThatThrownBy(() -> agentReportService.getMetrics("daily", EMP_ID, DATE))
//        .isInstanceOf(BusinessException.class)
//        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_DATA_NOT_FOUND);

    // given
    given(employeeRepository.existsById(EMP_ID)).willReturn(true);
    LocalDateTime targetTime = DATE.atStartOfDay();

    // 내 스냅샷이 없는 경우
    given(dailyAgentRepo.findByAgentIdAndStartAt(EMP_ID.longValue(), targetTime))
        .willReturn(Optional.empty());

    // when
    AgentMetricsResponse result = agentReportService.getMetrics("daily", EMP_ID, DATE);

    // then
    assertThat(result).isNull();
  }

  // --- [정상 로직 테스트] ---

  @Test
  @DisplayName("Daily 성과 지표(Metrics) 정상 조회 및 포맷팅 검증")
  void getMetrics_Daily_Success() {
    // given
    LocalDateTime targetTime = DATE.atStartOfDay();
    given(employeeRepository.existsById(EMP_ID)).willReturn(true);

//    BaseAgentSnapshot mySnap = mock(BaseAgentSnapshot.class);
    DailyAgentReportSnapshot mySnap = mock(DailyAgentReportSnapshot.class);
    given(mySnap.getStartAt()).willReturn(targetTime);
    given(mySnap.getEndAt()).willReturn(targetTime.plusDays(1));
    given(mySnap.getConsultCount()).willReturn(15);
    given(mySnap.getAvgDurationMinutes()).willReturn(10.75); // 10분 45초 예상

    given(dailyAgentRepo.findByAgentIdAndStartAt(EMP_ID.longValue(), targetTime))
        .willReturn(Optional.of(mySnap));
    given(dailyTotalRepo.findByStartAt(targetTime)).willReturn(Optional.empty()); // 팀 데이터는 없어도 진행됨

    // when
    AgentMetricsResponse result = agentReportService.getMetrics("daily", EMP_ID, DATE);

    // then
    assertThat(result.getMyConsultCount()).isEqualTo(15);
    assertThat(result.getMyAvgDuration()).isEqualTo("10:45"); // formatDuration 검증
    assertThat(result.getTeamAvgConsultCount()).isEqualTo(0.0); // Null 처리에 따른 기본값
  }

  @Test
  @DisplayName("카테고리 랭킹 그룹화 및 정렬 검증")
  void getCategories_GroupingAndSorting() {
    // given
    given(employeeRepository.existsById(EMP_ID)).willReturn(true);
    LocalDateTime targetTime = DATE.atStartOfDay();

    // 중분류는 다르지만 대분류가 같은 데이터들
    BaseAgentSnapshot.CategoryRanking r1 = new BaseAgentSnapshot.CategoryRanking("상품", "모바일", 10);
    BaseAgentSnapshot.CategoryRanking r2 = new BaseAgentSnapshot.CategoryRanking("상품", "인터넷", 5);
    BaseAgentSnapshot.CategoryRanking r3 = new BaseAgentSnapshot.CategoryRanking("결제", "카드", 20);

//    BaseAgentSnapshot mySnap = mock(BaseAgentSnapshot.class);
    DailyAgentReportSnapshot mySnap = mock(DailyAgentReportSnapshot.class);
    given(mySnap.getCategoryRanking()).willReturn(List.of(r1, r2, r3));
    given(mySnap.getStartAt()).willReturn(targetTime);

    given(dailyAgentRepo.findByAgentIdAndStartAt(EMP_ID.longValue(), targetTime))
        .willReturn(Optional.of(mySnap));

    // when
    List<CategoryRankingDto> result = agentReportService.getCategories("daily", EMP_ID, DATE);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("결제"); // 20건으로 1등
    assertThat(result.get(1).getName()).isEqualTo("상품"); // 10+5=15건으로 2등
    assertThat(result.get(1).getMediumCategories()).hasSize(2);
  }

  @Test
  @DisplayName("Monthly 조회 시 해당 월의 1일로 날짜 보정되는지 확인")
  void getSatisfaction_Monthly_DateAdjustment() {
    // given
    given(employeeRepository.existsById(EMP_ID)).willReturn(true);
    LocalDate middleOfMonth = LocalDate.of(2026, 3, 17);
    LocalDateTime firstDayOfMonth = LocalDate.of(2026, 3, 1).atStartOfDay();

    MonthlyAgentReportSnapshot mySnap = mock(MonthlyAgentReportSnapshot.class);

    given(mySnap.getStartAt()).willReturn(firstDayOfMonth);
    given(mySnap.getEndAt()).willReturn(firstDayOfMonth.plusMonths(1).minusSeconds(1));

    given(monthlyAgentRepo.findByAgentIdAndStartAt(EMP_ID.longValue(), firstDayOfMonth))
        .willReturn(Optional.of(mySnap));

    // when
    agentReportService.getSatisfaction("monthly", EMP_ID, middleOfMonth);

    // then
    verify(monthlyAgentRepo).findByAgentIdAndStartAt(eq(EMP_ID.longValue()), eq(firstDayOfMonth));
  }
}