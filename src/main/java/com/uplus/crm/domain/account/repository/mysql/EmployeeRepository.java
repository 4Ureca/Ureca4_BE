package com.uplus.crm.domain.account.repository.mysql;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uplus.crm.domain.account.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query(value = "SELECT e FROM Employee e " +
           "JOIN FETCH e.employeeDetail ed " +
           "JOIN FETCH ed.department d " +
           "JOIN FETCH ed.jobRole j " +
           "WHERE (:deptId IS NULL OR d.deptId = :deptId) " +
           "AND (:jobRoleId IS NULL OR j.jobRoleId = :jobRoleId) " +
           "AND (:isActive IS NULL OR e.isActive = :isActive) " +
           "AND (:keyword IS NULL OR e.name LIKE %:keyword% OR e.loginId LIKE %:keyword%)",
           countQuery = "SELECT COUNT(e) FROM Employee e " +
                        "JOIN e.employeeDetail ed " +
                        "WHERE (:deptId IS NULL OR ed.department.deptId = :deptId) " +
                        "AND (:jobRoleId IS NULL OR ed.jobRole.jobRoleId = :jobRoleId) " +
                        "AND (:isActive IS NULL OR e.isActive = :isActive) " +
                        "AND (:keyword IS NULL OR e.name LIKE %:keyword% OR e.loginId LIKE %:keyword%)")
    Page<Employee> searchEmployees(
            @Param("deptId") Integer deptId,
            @Param("jobRoleId") Integer jobRoleId,
            @Param("isActive") Boolean isActive,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    // 특정 직원의 상세 정보와 부서/역할까지 한 번에 가져오는 쿼리
    @Query("SELECT e FROM Employee e " +
           "JOIN FETCH e.employeeDetail ed " +
           "JOIN FETCH ed.department d " +
           "JOIN FETCH ed.jobRole j " +
           "WHERE e.empId = :empId")
    Optional<Employee> findByIdWithDetails(@Param("empId") Integer empId);
}

