package com.uplus.crm.domain.manual.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.manual.entity.Manual;

public interface ManualRepository extends JpaRepository<Manual, Integer> {
    // 특정 카테고리에서 현재 사용 중인 매뉴얼 찾기 (교체 및 활성화 로직용)
    Optional<Manual> findByCategoryPolicy_CategoryCodeAndIsActiveTrue(String categoryCode);

    // 카테고리별 전체 이력 조회
    List<Manual> findAllByCategoryPolicy_CategoryCodeOrderByCreatedAtDesc(String categoryCode);
   
    //추가: 전체 조회 (날짜 최신순) ⭐
    List<Manual> findAllByOrderByCreatedAtDesc();
}