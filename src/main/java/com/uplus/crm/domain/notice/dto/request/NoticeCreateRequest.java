package com.uplus.crm.domain.notice.dto.request;

import com.uplus.crm.domain.notice.entity.NoticeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoticeCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    private String content;

    @NotNull(message = "상태는 필수입니다.")
    private NoticeStatus status;

    private Boolean isPinned = false;

    private LocalDateTime visibleFrom;

    private LocalDateTime visibleTo;
}
