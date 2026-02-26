package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.summary.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Integer> {
    // findAll()은 JpaRepository에서 상속받아 사용
}