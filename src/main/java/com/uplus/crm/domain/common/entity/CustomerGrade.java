package com.uplus.crm.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerGrade {

  @Id
  @Column(name = "grade_code", length = 20, nullable = false)
  private String gradeCode;

  @Column(name = "grade_name", length = 20, nullable = false)
  private String gradeName;

  @Column(name = "priority_level")
  private Integer priorityLevel;

  public CustomerGrade(String gradeCode, String gradeName, Integer priorityLevel) {
    this.gradeCode = gradeCode;
    this.gradeName = gradeName;
    this.priorityLevel = priorityLevel;
  }
}