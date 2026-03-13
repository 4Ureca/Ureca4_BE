package com.uplus.crm.domain.account.repository.mysql;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.bookmark.entity.UserBookmark;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

    boolean existsByEmpIdAndManualId(Integer empId, Integer manualId);

    boolean existsByEmpIdAndConsultId(Integer empId, Long consultId);

    Optional<UserBookmark> findByEmpIdAndManualId(Integer empId, Integer manualId);

    Optional<UserBookmark> findByEmpIdAndConsultId(Integer empId, Long consultId);

    List<UserBookmark> findAllByEmpIdAndManualIdIsNotNullOrderByCreatedAtDesc(Integer empId);

    List<UserBookmark> findAllByEmpIdAndConsultIdIsNotNullOrderByCreatedAtDesc(Integer empId);
}