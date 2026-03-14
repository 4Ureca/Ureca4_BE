package com.uplus.crm.domain.summary.controller;

import com.uplus.crm.domain.summary.dto.request.ConsultationSearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDto;
import com.uplus.crm.domain.summary.service.ConsultationSearchService;
import com.uplus.crm.domain.summary.service.SummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

  private final SummaryService service;
  private final ConsultationSearchService consultationSearchService;

  @GetMapping
  public Page<ConsultationSummaryDto> list(
      @Valid @ModelAttribute @ParameterObject ConsultationSearchRequest request,
      @ParameterObject @PageableDefault(
          size = 20,
          sort = "consultedAt",
          direction = Sort.Direction.DESC
      ) Pageable pageable) {

    return consultationSearchService.search(request, pageable);
  }

  @GetMapping("/{consultId}")
  public ConsultationSummaryDetailResponse detail(
      @PathVariable Long consultId) {
    return service.getDetail(consultId);
  }
}
