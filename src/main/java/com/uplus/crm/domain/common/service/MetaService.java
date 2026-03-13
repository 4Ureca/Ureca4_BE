package com.uplus.crm.domain.common.service;

import com.uplus.crm.domain.common.dto.MetaDto.AgentDto;
import com.uplus.crm.domain.common.dto.MetaDto.CategoryDto;
import com.uplus.crm.domain.common.dto.MetaDto.GradeDto;
import com.uplus.crm.domain.common.dto.MetaDto.ProductDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskLevelDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskTypeDto;
import com.uplus.crm.domain.common.repository.ConsultationCategoryPolicyRepository;
import com.uplus.crm.domain.common.repository.CustomerGradeRepository;
import com.uplus.crm.domain.common.repository.EmployeeMetaRepository;
import com.uplus.crm.domain.common.repository.ProductAdditionalRepository;
import com.uplus.crm.domain.common.repository.ProductHomeRepository;
import com.uplus.crm.domain.common.repository.ProductMobileRepository;
import com.uplus.crm.domain.common.repository.RiskLevelPolicyRepository;
import com.uplus.crm.domain.common.repository.RiskTypePolicyRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class MetaService {

  private final EmployeeMetaRepository employeeRepository;
  private final ProductMobileRepository productMobileRepository;
  private final ProductHomeRepository productHomeRepository;
  private final ProductAdditionalRepository productAdditionalRepository;
  private final ConsultationCategoryPolicyRepository categoryRepository;
  private final CustomerGradeRepository gradeRepository;
  private final RiskTypePolicyRepository riskTypeRepository;
  private final RiskLevelPolicyRepository riskLevelRepository;

  public List<AgentDto> searchAgents(String name) {
    return employeeRepository.searchAgents(name)
        .stream().map(AgentDto::from).toList();
  }

  public List<ProductDto> searchProducts(String keyword) {
    List<ProductDto> result = new ArrayList<>();
    result.addAll(productMobileRepository
        .findTop20ByPlanNameContainingOrMobileCodeContaining(keyword, keyword)
        .stream().map(ProductDto::fromMobile).toList());
    result.addAll(productHomeRepository
        .findTop20ByProductNameContainingOrHomeCodeContaining(keyword, keyword)
        .stream().map(ProductDto::fromHome).toList());
    result.addAll(productAdditionalRepository
        .findTop20ByAdditionalNameContainingOrAdditionalCodeContaining(keyword, keyword)
        .stream().map(ProductDto::fromAdditional).toList());
    return result;
  }

  public List<CategoryDto> getCategories(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return categoryRepository.findByIsActiveTrueOrderBySortOrder()
          .stream().map(CategoryDto::from).toList();
    }
    return categoryRepository.searchByKeyword(keyword)
        .stream().map(CategoryDto::from).toList();
  }

  public List<GradeDto> getGrades() {
    return gradeRepository.findAllByOrderByPriorityLevel()
        .stream().map(GradeDto::from).toList();
  }

  public List<RiskTypeDto> getRiskTypes() {
    return riskTypeRepository.findByIsActiveTrue()
        .stream().map(RiskTypeDto::from).toList();
  }

  public List<RiskLevelDto> getRiskLevels() {
    return riskLevelRepository.findAllByOrderBySortOrder()
        .stream().map(RiskLevelDto::from).toList();
  }
}