package com.uplus.crm.domain.common.dto;

import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.common.entity.CustomerGrade;
import com.uplus.crm.domain.common.entity.ProductAdditional;
import com.uplus.crm.domain.common.entity.ProductHome;
import com.uplus.crm.domain.common.entity.ProductMobile;
import com.uplus.crm.domain.common.entity.RiskLevelPolicy;
import com.uplus.crm.domain.common.entity.RiskTypePolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;

public class MetaDto {

  public record AgentDto(Integer empId, String loginId, String name) {
    public static AgentDto from(Employee e) {
      return new AgentDto(e.getEmpId(), e.getLoginId(), e.getName());
    }
  }

  public record ProductDto(String code, String name, String type) {
    public static ProductDto fromMobile(ProductMobile p) {
      return new ProductDto(p.getMobileCode(), p.getPlanName(), "모바일");
    }
    public static ProductDto fromHome(ProductHome p) {
      return new ProductDto(p.getHomeCode(), p.getProductName(), "홈/인터넷");
    }
    public static ProductDto fromAdditional(ProductAdditional p) {
      return new ProductDto(p.getAdditionalCode(), p.getAdditionalName(), "부가서비스");
    }
  }

  public record CategoryDto(String categoryCode, String largeCategory,
                            String mediumCategory, String smallCategory) {
    public static CategoryDto from(ConsultationCategoryPolicy c) {
      return new CategoryDto(c.getCategoryCode(), c.getLargeCategory(),
          c.getMediumCategory(), c.getSmallCategory());
    }
  }

  public record GradeDto(String gradeCode, String gradeName, Integer priorityLevel) {
    public static GradeDto from(CustomerGrade g) {
      return new GradeDto(g.getGradeCode(), g.getGradeName(), g.getPriorityLevel());
    }
  }

  public record RiskTypeDto(String typeCode, String typeName) {
    public static RiskTypeDto from(RiskTypePolicy r) {
      return new RiskTypeDto(r.getTypeCode(), r.getTypeName());
    }
  }

  public record RiskLevelDto(String levelCode, String levelName) {
    public static RiskLevelDto from(RiskLevelPolicy r) {
      return new RiskLevelDto(r.getLevelCode(), r.getLevelName());
    }
  }
}