package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.EmployeeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeDetailRepository extends JpaRepository<EmployeeDetail, Integer> {
}