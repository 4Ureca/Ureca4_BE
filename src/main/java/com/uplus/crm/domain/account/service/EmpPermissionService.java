package com.uplus.crm.domain.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.account.dto.request.EmpPermissionRequestDto;
import com.uplus.crm.domain.account.dto.response.EmpPermissionListResponseDto;
import com.uplus.crm.domain.account.dto.response.EmpPermissionListResponseDto.EmpPermissionDto;
import com.uplus.crm.domain.account.entity.EmpPermission;
import com.uplus.crm.domain.account.repository.mysql.EmpPermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpPermissionService {

    private final EmpPermissionRepository empPermissionRepository;

    public EmpPermissionListResponseDto getEmployeePermissions(EmpPermissionRequestDto requestDto) {
    	Integer empId = requestDto.getEmpId();
        Integer isDeleted = requestDto.getIsDeleted();
        
        Boolean isDeletedFilter = (isDeleted == null) ? null : isDeleted == 1;

        List<EmpPermission> entities = empPermissionRepository.findByEmpIdWithPermission(empId, isDeletedFilter);

        List<EmpPermissionDto> dtos = entities.stream()
                .map(ep -> EmpPermissionDto.builder()
                        .empPermId(ep.getEmpPermId())
                        .empId(ep.getEmployee().getEmpId())
                        .permId(ep.getPermission().getPermId())
                        .permCode(ep.getPermission().getPermCode())
                        .permDesc(ep.getPermission().getPermDesc())
                        .assignedAt(ep.getAssignedAt())
                        .isDeleted(ep.getIsDeleted() ? 1 : 0) // Boolean -> Integer 변환
                        .build())
                .collect(Collectors.toList());

        return EmpPermissionListResponseDto.builder()
                .empPermissions(dtos)
                .build();
    }
}