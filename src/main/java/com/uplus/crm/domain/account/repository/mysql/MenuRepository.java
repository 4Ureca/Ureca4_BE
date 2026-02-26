package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    @Query("SELECT m.menuCode FROM Menu m " +
           "JOIN JobRoleMenu jrm ON m.menuId = jrm.menu.menuId " +
           "WHERE jrm.jobRole.jobRoleId = :jobRoleId " +
           "AND m.isDeleted = false " +
           "AND jrm.isDeleted = false")
    List<String> findMenuCodesByJobRoleId(@Param("jobRoleId") Integer jobRoleId);
}