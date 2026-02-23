package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByLoginId(String loginId);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);
}