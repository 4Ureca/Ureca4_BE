package com.uplus.crm.domain.account.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeAdminServiceImplTest {

  @InjectMocks
  private EmployeeAdminServiceImpl employeeAdminService;

  @Mock private EmployeeRepository employeeRepository;
  @Mock private EmployeeDetailRepository employeeDetailRepository;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private JobRoleRepository jobRoleRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private Employee employee;
  private Department department;
  private JobRole jobRole;

  @BeforeEach
  void setUp() {
    employee = Employee.builder()
        .empId(1)
        .loginId("login1")
        .name("홍길동")
        .isActive(true)
        .build();

    department = Department.builder()
        .deptId(1)
        .deptName("영업팀")
        .build();

    jobRole = JobRole.builder()
        .jobRoleId(1)
        .roleName("매니저")
        .build();
  }

  @Test
  @DisplayName("직원 생성 성공")
  void createEmployee_success() {

    // given
    EmployeeCreateRequestDto request = EmployeeCreateRequestDto.builder()
        .loginId("login1")
        .password("1234")
        .name("홍길동")
        .email("test@test.com")
        .phone("01012341234")
        .birth(LocalDate.of(1990, 1, 1))
        .gender("M")
        .deptId(1)
        .jobRoleId(1)
        .joinedAt(LocalDate.now())
        .build();

    when(passwordEncoder.encode("1234")).thenReturn("encodedPw");
    when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
    when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
    when(jobRoleRepository.findById(1)).thenReturn(Optional.of(jobRole));

    // when
    var response = employeeAdminService.createEmployee(request);

    // then
    assertThat(response.getEmpId()).isEqualTo(1);
    assertThat(response.getLoginId()).isEqualTo("login1");
    assertThat(response.getName()).isEqualTo("홍길동");
    assertThat(response.getDeptName()).isEqualTo("영업팀");
    assertThat(response.getRoleName()).isEqualTo("매니저");

    // 비즈니스 로직 호출 검증
    verify(passwordEncoder).encode("1234");
    verify(employeeRepository).save(any(Employee.class));
    verify(employeeDetailRepository).save(any(EmployeeDetail.class));
  }

  @Test
  @DisplayName("직원 생성 - 존재하지 않는 부서 예외")
  void createEmployee_departmentNotFound() {

    EmployeeCreateRequestDto request = new EmployeeCreateRequestDto();
    request.setLoginId("login1");
    request.setPassword("1234");
    request.setName("홍길동");
    request.setEmail("test@test.com");
    request.setPhone("010");
    request.setBirth(LocalDate.of(1990,1,1));
    request.setGender("M");
    request.setDeptId(1);
    request.setJobRoleId(1);
    request.setJoinedAt(LocalDate.now());

    when(employeeRepository.save(any())).thenReturn(employee);
    when(departmentRepository.findById(1)).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        employeeAdminService.createEmployee(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.DEPARTMENT_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("직원 상태 비활성화 성공")
  void updateEmployeeStatus_success() {

    when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

    EmployeeStatusUpdateRequestDto request =
        new EmployeeStatusUpdateRequestDto(false);

    var response = employeeAdminService.updateEmployeeStatus(1, request);

    assertThat(response.getEmpId()).isEqualTo(1);
    assertThat(response.getIsActive()).isFalse();
    verify(employeeRepository).findById(1);
  }

  @Test
  @DisplayName("직원 상태 변경 - 존재하지 않는 직원 예외")
  void updateEmployeeStatus_notFound() {

    when(employeeRepository.findById(1)).thenReturn(Optional.empty());

    EmployeeStatusUpdateRequestDto request =
        new EmployeeStatusUpdateRequestDto(false);

    assertThatThrownBy(() ->
        employeeAdminService.updateEmployeeStatus(1, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.EMPLOYEE_NOT_FOUND.getMessage());
  }

}