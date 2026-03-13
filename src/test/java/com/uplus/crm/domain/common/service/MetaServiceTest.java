package com.uplus.crm.domain.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.common.dto.MetaDto.AgentDto;
import com.uplus.crm.domain.common.dto.MetaDto.CategoryDto;
import com.uplus.crm.domain.common.dto.MetaDto.GradeDto;
import com.uplus.crm.domain.common.dto.MetaDto.ProductDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskLevelDto;
import com.uplus.crm.domain.common.dto.MetaDto.RiskTypeDto;
import com.uplus.crm.domain.common.entity.CustomerGrade;
import com.uplus.crm.domain.common.entity.ProductAdditional;
import com.uplus.crm.domain.common.entity.ProductHome;
import com.uplus.crm.domain.common.entity.ProductMobile;
import com.uplus.crm.domain.common.entity.RiskLevelPolicy;
import com.uplus.crm.domain.common.entity.RiskTypePolicy;
import com.uplus.crm.domain.common.repository.ConsultationCategoryPolicyRepository;
import com.uplus.crm.domain.common.repository.CustomerGradeRepository;
import com.uplus.crm.domain.common.repository.EmployeeMetaRepository;
import com.uplus.crm.domain.common.repository.ProductAdditionalRepository;
import com.uplus.crm.domain.common.repository.ProductHomeRepository;
import com.uplus.crm.domain.common.repository.ProductMobileRepository;
import com.uplus.crm.domain.common.repository.RiskLevelPolicyRepository;
import com.uplus.crm.domain.common.repository.RiskTypePolicyRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetaServiceTest {

  @InjectMocks
  private MetaService metaService;

  @Mock private EmployeeMetaRepository employeeRepository;
  @Mock private ProductMobileRepository productMobileRepository;
  @Mock private ProductHomeRepository productHomeRepository;
  @Mock private ProductAdditionalRepository productAdditionalRepository;
  @Mock private ConsultationCategoryPolicyRepository categoryRepository;
  @Mock private CustomerGradeRepository gradeRepository;
  @Mock private RiskTypePolicyRepository riskTypeRepository;
  @Mock private RiskLevelPolicyRepository riskLevelRepository;

  @Test
  @DisplayName("searchAgents - 사원 정보를 AgentDto로 변환해서 반환한다")
  void searchAgents_returnsMappedDto() {
    Employee employee = Employee.builder()
        .empId(7)
        .loginId("agent007")
        .name("김상담")
        .build();
    given(employeeRepository.searchAgents("김")).willReturn(List.of(employee));

    List<AgentDto> result = metaService.searchAgents("김");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).empId()).isEqualTo(7);
    assertThat(result.get(0).loginId()).isEqualTo("agent007");
    assertThat(result.get(0).name()).isEqualTo("김상담");
  }

  @Test
  @DisplayName("searchProducts - 모바일/홈/부가서비스 결과를 모두 합쳐 반환한다")
  void searchProducts_mergesAllProductTypes() {
    ProductMobile mobile = new ProductMobile(
        "M001", "5G", "5G 프리미어", 89000, 0,
        "무제한", null, "무제한", "무제한", null, null, "일반", null);
    ProductHome home = new ProductHome(
        "H001", "인터넷", "기가 인터넷", 39000, "1Gbps", "3년", null);
    ProductAdditional additional = new ProductAdditional(
        "A001", "부가", "통화연결음", 1100, null);

    given(productMobileRepository.findTop20ByPlanNameContainingOrMobileCodeContaining("요금", "요금"))
        .willReturn(List.of(mobile));
    given(productHomeRepository.findTop20ByProductNameContainingOrHomeCodeContaining("요금", "요금"))
        .willReturn(List.of(home));
    given(productAdditionalRepository.findTop20ByAdditionalNameContainingOrAdditionalCodeContaining("요금", "요금"))
        .willReturn(List.of(additional));

    List<ProductDto> result = metaService.searchProducts("요금");

    assertThat(result).hasSize(3);
    assertThat(result)
        .extracting(ProductDto::type)
        .containsExactly("모바일", "홈/인터넷", "부가서비스");
  }

  @Test
  @DisplayName("getCategories - 키워드가 없으면 활성 카테고리 정렬 조회를 사용한다")
  void getCategories_withoutKeyword_usesActiveQuery() {
    ConsultationCategoryPolicy category = mock(ConsultationCategoryPolicy.class);
    given(category.getCategoryCode()).willReturn("C001");
    given(category.getLargeCategory()).willReturn("요금");
    given(category.getMediumCategory()).willReturn("청구");
    given(category.getSmallCategory()).willReturn("문의");
    given(categoryRepository.findByIsActiveTrueOrderBySortOrder()).willReturn(List.of(category));

    List<CategoryDto> result = metaService.getCategories(" ");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).categoryCode()).isEqualTo("C001");
    assertThat(result.get(0).largeCategory()).isEqualTo("요금");
  }

  @Test
  @DisplayName("getCategories - 키워드가 있으면 키워드 검색을 사용한다")
  void getCategories_withKeyword_usesSearchQuery() {
    ConsultationCategoryPolicy category = mock(ConsultationCategoryPolicy.class);
    given(category.getCategoryCode()).willReturn("C010");
    given(category.getLargeCategory()).willReturn("해지");
    given(category.getMediumCategory()).willReturn("방어");
    given(category.getSmallCategory()).willReturn("정책");
    given(categoryRepository.searchByKeyword("해지")).willReturn(List.of(category));

    List<CategoryDto> result = metaService.getCategories("해지");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).categoryCode()).isEqualTo("C010");
  }

  @Test
  @DisplayName("getGrades/getRiskTypes/getRiskLevels - 각 메타를 DTO로 반환한다")
  void getMetaLists_returnsMappedDtos() {
    given(gradeRepository.findAllByOrderByPriorityLevel())
        .willReturn(List.of(new CustomerGrade("VIP", "우수", 1)));
    given(riskTypeRepository.findByIsActiveTrue())
        .willReturn(List.of(new RiskTypePolicy("RT1", "스크립트 미준수", true)));
    given(riskLevelRepository.findAllByOrderBySortOrder())
        .willReturn(List.of(new RiskLevelPolicy("RL1", "높음", 1, "치명도 높음")));

    List<GradeDto> grades = metaService.getGrades();
    List<RiskTypeDto> riskTypes = metaService.getRiskTypes();
    List<RiskLevelDto> riskLevels = metaService.getRiskLevels();

    assertThat(grades.get(0).gradeCode()).isEqualTo("VIP");
    assertThat(riskTypes.get(0).typeCode()).isEqualTo("RT1");
    assertThat(riskLevels.get(0).levelCode()).isEqualTo("RL1");
  }
}
