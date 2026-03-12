package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.CustomerGrade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerGradeRepository extends JpaRepository<CustomerGrade, String> {
  List<CustomerGrade> findAllByOrderByPriorityLevel();
}