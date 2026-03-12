package com.uplus.crm.domain.consultation.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsultationRawTextRepository extends JpaRepository<ConsultationRawText, Long> {

    Optional<ConsultationRawText> findFirstByConsultId(Long consultId);

    List<ConsultationRawText> findByConsultIdIn(Collection<Long> consultIds);
}
