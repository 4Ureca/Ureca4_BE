package com.uplus.crm.domain.notice.controller;

import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.request.NoticeUpdateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeListResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notice", description = "공지사항 CRUD API")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 생성")
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        NoticeResponse response = noticeService.createNotice(empId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "공지 목록 조회")
    @GetMapping
    public ResponseEntity<NoticeListResponse> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(noticeService.getNotices(page, size));
    }

    @Operation(summary = "공지 상세 조회")
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Integer noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    @Operation(summary = "공지 수정")
    @PutMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Integer noticeId,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request));
    }

    @Operation(summary = "공지 삭제(소프트 삭제)")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Map<String, String>> deleteNotice(@PathVariable Integer noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다."));
    }
}
