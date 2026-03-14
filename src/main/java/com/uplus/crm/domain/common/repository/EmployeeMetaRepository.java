package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.account.entity.Employee;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeMetaRepository extends JpaRepository<Employee, Integer> {

  @Query("""
        SELECT e FROM Employee e
        JOIN e.employeeDetail d
        JOIN d.department dept
        JOIN d.jobRole role
        WHERE e.name LIKE %:name%
        AND e.isActive = true
        AND dept.deptName = '상담부'
        AND role.roleName = '상담사'
        """)
  List<Employee> searchAgents(@Param("name") String name);
}
