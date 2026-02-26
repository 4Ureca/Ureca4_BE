package com.uplus.crm.domain.account.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.DepartmentRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeDetailRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.JobRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEmployeeServiceTest {

    @InjectMocks
    private AdminEmployeeService adminEmployeeService;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeDetailRepository employeeDetailRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private JobRoleRepository jobRoleRepository;

    private Employee mockEmployee;
    private Department mockDept;
    private JobRole mockRole;
    private EmployeeDetail mockDetail;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .empId(1)
                .loginId("EMP001")
                .password("encodedPassword")
                .name("홍길동")
                .email("hong@lgup.com")
                .isActive(true)
                .build();

        mockDept = Department.builder()
                .deptId(10)
                .deptName("고객서비스팀")
                .build();

        mockRole = JobRole.builder()
                .jobRoleId(20)
                .roleName("상담사")
                .isActive(true)
                .build();

        mockDetail = EmployeeDetail.builder()
                .empId(1)
                .employee(mockEmployee)
                .department(mockDept)
                .jobRole(mockRole)
                .build();
    }

    // ─────────────────────────────────────────────
    // PUT /admin/employees/{id}
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /admin/employees/{id} - 직원 정보 수정")
    class UpdateEmployee {

        @Test
        @DisplayName("성공 - 기존 EmployeeDetail이 있는 경우")
        void success_existingDetail() {
            // given
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("김철수")
                    .email("kim@lgup.com")
                    .phone("010-9999-8888")
                    .birth("1985-03-20")
                    .gender("M")
                    .deptId(10)
                    .jobRoleId(20)
                    .joinedAt("2020-01-15")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("kim@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(20)).willReturn(Optional.of(mockRole));
            given(employeeDetailRepository.findById(1L)).willReturn(Optional.of(mockDetail));
            given(employeeDetailRepository.save(any())).willAnswer(inv -> {
                EmployeeDetail d = inv.getArgument(0);
                return d;
            });

            // when
            AdminEmployeeUpdateResponseDto res = adminEmployeeService.updateEmployee(1, req);

            // then
            assertThat(res.getEmpId()).isEqualTo(1);
            assertThat(res.getName()).isEqualTo("김철수");
            assertThat(res.getEmail()).isEqualTo("kim@lgup.com");
            assertThat(res.getDeptName()).isEqualTo("고객서비스팀");
            assertThat(res.getRoleName()).isEqualTo("상담사");
        }

        @Test
        @DisplayName("성공 - EmployeeDetail이 없어 새로 생성되는 경우")
        void success_newDetail() {
            // given
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("박영희")
                    .email("park@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("park@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(20)).willReturn(Optional.of(mockRole));
            given(employeeDetailRepository.findById(1L)).willReturn(Optional.empty());
            given(employeeDetailRepository.save(any())).willReturn(mockDetail);

            // when
            AdminEmployeeUpdateResponseDto res = adminEmployeeService.updateEmployee(1, req);

            // then
            assertThat(res.getName()).isEqualTo("박영희");
            then(employeeDetailRepository).should().save(any(EmployeeDetail.class));
        }

        @Test
        @DisplayName("성공 - nullable 날짜 필드가 없어도 정상 처리")
        void success_withoutOptionalDates() {
            // given
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(20)).willReturn(Optional.of(mockRole));
            given(employeeDetailRepository.findById(1L)).willReturn(Optional.of(mockDetail));
            given(employeeDetailRepository.save(any())).willReturn(mockDetail);

            // when & then
            assertThatCode(() -> adminEmployeeService.updateEmployee(1, req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("실패 - 요청 본문 null")
        void fail_nullRequest() {
            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 이름 blank")
        void fail_blankName() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("")
                    .email("hong@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - deptId null")
        void fail_nullDeptId() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .jobRoleId(20)
                    .build();

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 직원 존재하지 않음")
        void fail_employeeNotFound() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(999)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(999, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 이메일 중복 (다른 직원이 이미 사용)")
        void fail_emailDuplicate() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("taken@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("taken@lgup.com", 1)).willReturn(true);

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_EMAIL));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 부서 ID")
        void fail_deptNotFound() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .deptId(999)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(999)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 역할 ID")
        void fail_roleNotFound() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .deptId(10)
                    .jobRoleId(999)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(999)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 생년월일 날짜 형식 오류 (yyyy/MM/dd)")
        void fail_invalidBirthFormat() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .birth("1990/05/15")
                    .deptId(10)
                    .jobRoleId(20)
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(20)).willReturn(Optional.of(mockRole));

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 입사일 날짜 형식 오류 (잘못된 날짜 값)")
        void fail_invalidJoinedAtFormat() {
            AdminEmployeeUpdateRequestDto req = AdminEmployeeUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .deptId(10)
                    .jobRoleId(20)
                    .joinedAt("not-a-date")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);
            given(departmentRepository.findById(10)).willReturn(Optional.of(mockDept));
            given(jobRoleRepository.findById(20)).willReturn(Optional.of(mockRole));

            assertThatThrownBy(() -> adminEmployeeService.updateEmployee(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }
    }
}
