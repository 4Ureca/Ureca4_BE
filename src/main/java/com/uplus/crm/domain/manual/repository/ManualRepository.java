package com.uplus.crm.domain.manual.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.manual.entity.Manual;

public interface ManualRepository extends JpaRepository<Manual, Integer> {
}
