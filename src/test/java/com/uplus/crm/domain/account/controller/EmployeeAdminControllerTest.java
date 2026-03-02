package com.uplus.crm.domain.account.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.service.EmployeeAdminService;
import com.uplus.crm.domain.account.service.EmployeeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeAdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private EmployeeAdminService employeeAdminService;
  @MockitoBean
  private EmployeeService employeeService;
  @MockitoBean
  private JwtUtil jwtUtil;                         // JwtAuthFilter 의존성
  @MockitoBean
  private EmployeeRepository employeeRepository;   // JwtAuthFilter 의존성

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("직원 계정 생성 성공")
  void createEmployee_success() throws Exception {

    EmployeeCreateRequestDto request = EmployeeCreateRequestDto.builder()
        .loginId("login1")
        .password("1234")
        .name("홍길동")
        .email("test@test.com")
        .phone("01012341234")
        .birth(LocalDate.of(1990,1,1))
        .gender("M")
        .deptId(1)
        .jobRoleId(1)
        .joinedAt(LocalDate.now())
        .build();

    EmployeeCreateResponseDto response =
        new EmployeeCreateResponseDto(
            1,
            "login1",
            "홍길동",
            "영업팀",
            "매니저",
            LocalDateTime.now()
        );

    when(employeeAdminService.createEmployee(any()))
        .thenReturn(response);

    mockMvc.perform(post("/admin/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.empId").value(1))
        .andExpect(jsonPath("$.loginId").value("login1"))
        .andExpect(jsonPath("$.name").value("홍길동"));
  }

  @Test
  @DisplayName("직원 상태 변경 성공")
  void updateEmployeeStatus_success() throws Exception {

    EmployeeStatusUpdateRequestDto request =
        new EmployeeStatusUpdateRequestDto(false);

    EmployeeStatusUpdateResponseDto response =
        new EmployeeStatusUpdateResponseDto(
            1,
            "홍길동",
            false,
            "계정이 비활성화되었습니다."
        );

    when(employeeAdminService.updateEmployeeStatus(Mockito.eq(1), any()))
        .thenReturn(response);

    mockMvc.perform(patch("/admin/employees/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isActive").value(false));
  }

}