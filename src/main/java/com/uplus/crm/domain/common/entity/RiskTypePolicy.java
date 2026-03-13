package com.uplus.crm.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risk_type_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskTypePolicy {

  @Id
  @Column(name = "type_code", length = 20, nullable = false)
  private String typeCode;

  @Column(name = "type_name", length = 50, nullable = false)
  private String typeName;

  @Column(name = "is_active")
  private Boolean isActive = true;

  public RiskTypePolicy(String typeCode, String typeName, Boolean isActive) {
    this.typeCode = typeCode;
    this.typeName = typeName;
    this.isActive = isActive;
  }
}