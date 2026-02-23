package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  // 사번(로그인 ID) 중복 체크
  boolean existsByLoginId(String loginId);

  Optional<Employee> findByLoginId(String loginId);

}
