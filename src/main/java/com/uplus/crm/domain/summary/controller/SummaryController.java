package com.uplus.crm.domain.summary.controller;

import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

  private final SummaryService service;

  @GetMapping
  public Page<ConsultationSummaryListResponse> list(
      @ParameterObject @PageableDefault(
          size = 20,
          sort = "consultId",
          direction = Sort.Direction.DESC
      ) Pageable pageable) {

    return service.getList(pageable);
  }

  @GetMapping("/{consultId}")
  public ConsultationSummaryDetailResponse detail(
      @PathVariable Long consultId) {

    return service.getDetail(consultId);
  }
}