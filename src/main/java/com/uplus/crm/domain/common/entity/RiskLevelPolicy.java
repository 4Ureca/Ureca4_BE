package com.uplus.crm.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risk_level_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskLevelPolicy {

  @Id
  @Column(name = "level_code", length = 20, nullable = false)
  private String levelCode;

  @Column(name = "level_name", length = 50, nullable = false)
  private String levelName;

  @Column(name = "sort_order")
  private Integer sortOrder;

  @Column(name = "description", nullable = false)
  private String description;

  public RiskLevelPolicy(String levelCode, String levelName, Integer sortOrder, String description) {
    this.levelCode = levelCode;
    this.levelName = levelName;
    this.sortOrder = sortOrder;
    this.description = description;
  }
}