package com.uplus.crm.domain.notice.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeListResponse {
    private List<NoticeResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
