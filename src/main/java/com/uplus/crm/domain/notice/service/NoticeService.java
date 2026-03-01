package com.uplus.crm.domain.notice.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.request.NoticeUpdateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeListResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public NoticeResponse createNotice(Integer empId, NoticeCreateRequest request) {
        validateVisiblePeriod(request.getVisibleFrom(), request.getVisibleTo());

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .employee(employee)
                .isPinned(Boolean.TRUE.equals(request.getIsPinned()))
                .status(request.getStatus())
                .visibleFrom(request.getVisibleFrom())
                .visibleTo(request.getVisibleTo())
                .build();

        return NoticeResponse.from(noticeRepository.save(notice));
    }

    public NoticeListResponse getNotices(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "page는 0 이상, size는 1 이상이어야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"))
        );

        Page<Notice> noticePage = noticeRepository.findAllByStatusNot(NoticeStatus.DELETED, pageRequest);

        List<NoticeResponse> content = noticePage.getContent().stream()
                .map(NoticeResponse::from)
                .toList();

        return NoticeListResponse.builder()
                .content(content)
                .totalElements(noticePage.getTotalElements())
                .totalPages(noticePage.getTotalPages())
                .page(noticePage.getNumber())
                .size(noticePage.getSize())
                .build();
    }

    @Transactional
    public NoticeResponse getNotice(Integer noticeId) {
        Notice notice = getNoticeOrThrow(noticeId);
        notice.increaseViewCount();
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse updateNotice(Integer noticeId, NoticeUpdateRequest request) {
        validateVisiblePeriod(request.getVisibleFrom(), request.getVisibleTo());

        Notice notice = getNoticeOrThrow(noticeId);
        notice.update(
                request.getTitle(),
                request.getContent(),
                request.getIsPinned(),
                request.getStatus(),
                request.getVisibleFrom(),
                request.getVisibleTo()
        );

        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Integer noticeId) {
        Notice notice = getNoticeOrThrow(noticeId);
        notice.update(
                notice.getTitle(),
                notice.getContent(),
                notice.getIsPinned(),
                NoticeStatus.DELETED,
                notice.getVisibleFrom(),
                notice.getVisibleTo()
        );
    }

    private Notice getNoticeOrThrow(Integer noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    private void validateVisiblePeriod(java.time.LocalDateTime visibleFrom,
                                       java.time.LocalDateTime visibleTo) {
        if (visibleFrom != null && visibleTo != null && visibleFrom.isAfter(visibleTo)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "노출 시작 일시는 노출 종료 일시보다 늦을 수 없습니다.");
        }
    }
}
