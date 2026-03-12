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
@Table(name = "product_additional")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductAdditional {

  @Id
  @Column(name = "additional_code", length = 30, nullable = false)
  private String additionalCode;

  @Column(name = "category", length = 20, nullable = false)
  private String category;

  @Column(name = "additional_name", length = 100, nullable = false)
  private String additionalName;

  @Column(name = "monthly_fee", nullable = false)
  private Integer monthlyFee = 0;

  @Column(name = "description")
  private String description;

  public ProductAdditional(
      String additionalCode,
      String category,
      String additionalName,
      Integer monthlyFee,
      String description
  ) {
    this.additionalCode = additionalCode;
    this.category = category;
    this.additionalName = additionalName;
    this.monthlyFee = monthlyFee;
    this.description = description;
  }
}