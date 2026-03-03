package com.uplus.crm.domain.consultation.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConsultationResultRepository extends JpaRepository<ConsultationResult, Long> {

    @Query(value = "SELECT * FROM consultation_results ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<ConsultationResult> findOneRandom();
}
