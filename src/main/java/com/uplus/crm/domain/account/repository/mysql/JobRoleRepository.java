package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.JobRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRoleRepository extends JpaRepository<JobRole, Integer> {

  List<JobRole> findAllByOrderByJobRoleIdAsc();
}
