package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.EmpPermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpPermissionRepository extends JpaRepository<EmpPermission, Long> {

  // 특정 직원 권한 목록 조회
  List<EmpPermission> findByEmployee_EmpIdAndIsDeletedFalse(Long empId);

  // 특정 직원 권한 전체 삭제 (soft delete 대신 물리삭제 쓸 수도 있음)
  void deleteByEmployee_EmpId(Long empId);

  @Modifying
  @Query("""
    UPDATE EmpPermission ep
       SET ep.isDeleted = true
     WHERE ep.employee.empId = :empId
       AND ep.isDeleted = false
""")
  void softDeleteByEmployeeId(@Param("empId") Long empId);
}
