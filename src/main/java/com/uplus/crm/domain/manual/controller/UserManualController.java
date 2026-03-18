package com.uplus.crm.domain.manual.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.service.ManualService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Counselor - Manual", description = "상담사용 매뉴얼 조회 API")
@RestController
@RequestMapping("/manuals")
@RequiredArgsConstructor
public class UserManualController {

    private final ManualService manualService;

    @Operation(summary = "매뉴얼 이력 조회 (페이징 & 필터)", 
               description = "카테고리 및 활성 상태('활성화'/'비활성화')에 따라 상담사용 매뉴얼 이력을 조회합니다.")
    @GetMapping("/history") 
    public ResponseEntity<Page<ManualResponse>> getManualHistory(
            @Parameter(description = "카테고리 코드 (선택)", example = "M_ADD_01")
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            
            @Parameter(description = "상태 필터 ('활성화', '비활성화', 또는 '전체')", example = "활성화")
            @RequestParam(name = "status", required = false) String status, 
            
            @ParameterObject Pageable pageable 
    ) {
        Boolean isActive = mapStatusToBoolean(status);
        
        return ResponseEntity.ok(manualService.getHistory(categoryCode, isActive, pageable));
    }

    /**
     * 프론트엔드 한글 상태값을 Boolean으로 확실하게 매핑하는 헬퍼 메서드 🥊
     */
    private Boolean mapStatusToBoolean(String status) {
        if ("활성화".equals(status)) {
            return true;
        }
        if ("비활성화".equals(status)) {
            return false;
        }
        return null; 
    }
}