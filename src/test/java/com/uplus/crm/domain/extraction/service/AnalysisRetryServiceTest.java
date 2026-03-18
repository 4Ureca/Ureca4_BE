package com.uplus.crm.domain.extraction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.uplus.crm.domain.extraction.entity.EventStatus;
import com.uplus.crm.domain.extraction.entity.ExcellentEventStatus;
import com.uplus.crm.domain.extraction.entity.ResultEventStatus;
import com.uplus.crm.domain.extraction.repository.ExcellentEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.ResultEventStatusRepository;

@ExtendWith(MockitoExtension.class)
class AnalysisRetryServiceTest {

    @InjectMocks
    private AnalysisRetryService retryService;

    @Mock
    private ResultEventStatusRepository resultRepository;

    @Mock
    private ExcellentEventStatusRepository excellentRepository;

    @Test
    @DisplayName("관리자가 수동 재처리 요청 시, 요약 및 채점 상태가 REQUESTED로 초기화되어야 한다.")
    void retryConsultations_Success() {
        // given  (상황 설정)
        List<Long> consultIds = List.of(1L, 2L);
        
        // 가짜 엔티티 생성 (실패 상태였다고 가정)
        ResultEventStatus resultEntity = ResultEventStatus.builder().consultId(1L).categoryCode("OTB").build();
        ExcellentEventStatus excellentEntity = ExcellentEventStatus.builder().consultId(1L).build();
        
        // 리포지토리 동작 정의
        given(resultRepository.findAllByConsultIdIn(anyList())).willReturn(List.of(resultEntity));
        given(excellentRepository.findAllByConsultIdIn(anyList())).willReturn(List.of(excellentEntity));

        // SecurityContext 모킹 (로그에 찍힐 관리자 아이디 설정) 
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(authentication.getName()).willReturn("admin_seunghyeok");

            // when 🥊 (실행)
            retryService.retryConsultations(consultIds);

            // then 🥊 (검증)
            // 1. 상태가 REQUESTED로 바뀌었는가?
            assertThat(resultEntity.getStatus()).isEqualTo(EventStatus.REQUESTED);
            assertThat(resultEntity.getRetryCount()).isEqualTo(0);
            assertThat(resultEntity.getFailReason()).isNull();

            assertThat(excellentEntity.getStatus()).isEqualTo(EventStatus.REQUESTED);
            assertThat(excellentEntity.getRetryCount()).isEqualTo(0);
            assertThat(excellentEntity.getFailReason()).isNull();

            // 2. 리포지토리 조회가 실제로 일어났는가?
            verify(resultRepository, times(1)).findAllByConsultIdIn(consultIds);
            verify(excellentRepository, times(1)).findAllByConsultIdIn(consultIds);
        }
    }
}