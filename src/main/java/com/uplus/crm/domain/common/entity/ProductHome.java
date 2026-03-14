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
@Table(name = "product_home")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductHome {

  @Id
  @Column(name = "home_code", length = 30, nullable = false)
  private String homeCode;

  @Column(name = "category", length = 20, nullable = false)
  private String category;

  @Column(name = "product_name", length = 100, nullable = false)
  private String productName;

  @Column(name = "monthly_fee", nullable = false)
  private Integer monthlyFee;

  @Column(name = "speed_limit", length = 20)
  private String speedLimit;

  @Column(name = "contract_period", length = 20)
  private String contractPeriod;

  @Column(name = "description")
  private String description;

  public ProductHome(
      String homeCode,
      String category,
      String productName,
      Integer monthlyFee,
      String speedLimit,
      String contractPeriod,
      String description
  ) {
    this.homeCode = homeCode;
    this.category = category;
    this.productName = productName;
    this.monthlyFee = monthlyFee;
    this.speedLimit = speedLimit;
    this.contractPeriod = contractPeriod;
    this.description = description;
  }
}