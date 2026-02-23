package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

}
