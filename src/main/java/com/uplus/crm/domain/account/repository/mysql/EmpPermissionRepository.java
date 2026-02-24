package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.EmpPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmpPermissionRepository extends JpaRepository<EmpPermission, Integer> {

    // fetch join을 통해 Permission 정보를 한 번에 가져옵니다.
    @Query("SELECT ep FROM EmpPermission ep " +
           "JOIN FETCH ep.permission " +
           "WHERE ep.employee.empId = :empId " +
           "AND (:isDeleted IS NULL OR ep.isDeleted = :isDeleted)")
    List<EmpPermission> findByEmpIdWithPermission(@Param("empId") Integer empId, 
                                                   @Param("isDeleted") Boolean isDeleted);
    
    @Query("SELECT p.permCode FROM EmpPermission ep " +
    	       "JOIN ep.permission p " +
    	       "WHERE ep.employee.empId = :empId AND ep.isDeleted = false")
    List<String> findPermCodesByEmpId(@Param("empId") Integer empId);
}