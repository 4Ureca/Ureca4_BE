package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

  List<Department> findAllByOrderByDeptIdAsc();
}
