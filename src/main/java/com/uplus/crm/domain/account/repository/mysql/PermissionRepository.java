package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer>{
  List<Permission> findAllById(List<Integer> ids);
}
