package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.summary.entity.FilterCustom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterCustomRepository extends JpaRepository<FilterCustom, Integer> {

    List<FilterCustom> findAllByFilterGroup_FilterGroupId(Integer filterGroupId);

    void deleteAllByFilterGroup_FilterGroupId(Integer filterGroupId);
}