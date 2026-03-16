package com.uplus.crm.domain.common.controller;

import com.uplus.crm.domain.common.dto.MetaDto.AgentDto;
import com.uplus.crm.domain.common.dto.MetaDto.AnalysisCodeDto;
import com.uplus.crm.domain.common.dto.MetaDto.CategoryDto;
import com.uplus.crm.domain.common.dto.MetaDto.GradeDto;
import com.uplus.crm.domain.common.dto.MetaDto.ProductDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskLevelDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskTypeDto;
import com.uplus.crm.domain.common.service.MetaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

  private final MetaService metaService;

  @GetMapping("/agents")
  public List<AgentDto> searchAgents(@RequestParam(required = false, defaultValue = "") String name) {
    return metaService.searchAgents(name);
  }

  @GetMapping("/products")
  public List<ProductDto> searchProducts(@RequestParam(required = false, defaultValue = "") String keyword) {
    return metaService.searchProducts(keyword);
  }

  @GetMapping("/categories")
  public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "") String keyword) {
    return metaService.getCategories(keyword);
  }

  @GetMapping("/grades")
  public List<GradeDto> getGrades() {
    return metaService.getGrades();
  }

  @GetMapping("/risk-types")
  public List<RiskTypeDto> getRiskTypes() {
    return metaService.getRiskTypes();
  }

  @GetMapping("/risk-levels")
  public List<RiskLevelDto> getRiskLevels() {
    return metaService.getRiskLevels();
  }

  @GetMapping("/analysis-codes")
  public List<AnalysisCodeDto> getAnalysisCodes(
      @RequestParam(required = false) String classification) {
    return metaService.getAnalysisCodes(classification);
  }
}