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
@Table(name = "product_mobile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMobile {

  @Id
  @Column(name = "mobile_code", length = 30, nullable = false)
  private String mobileCode;

  @Column(name = "category", length = 20, nullable = false)
  private String category;

  @Column(name = "plan_name", length = 100, nullable = false)
  private String planName;

  @Column(name = "monthly_fee", nullable = false)
  private Integer monthlyFee;

  @Column(name = "contract_fee")
  private Integer contractFee;

  @Column(name = "data_amount", length = 20, nullable = false)
  private String dataAmount;

  @Column(name = "qos_speed", length = 20)
  private String qosSpeed;

  @Column(name = "voice", length = 20, nullable = false)
  private String voice = "무제한";

  @Column(name = "sms", length = 20, nullable = false)
  private String sms = "무제한";

  @Column(name = "sharing_data", length = 20)
  private String sharingData;

  @Column(name = "benefits", length = 200)
  private String benefits;

  @Column(name = "target_group", length = 30, nullable = false)
  private String targetGroup = "일반";

  @Column(name = "description")
  private String description;

  public ProductMobile(
      String mobileCode,
      String category,
      String planName,
      Integer monthlyFee,
      Integer contractFee,
      String dataAmount,
      String qosSpeed,
      String voice,
      String sms,
      String sharingData,
      String benefits,
      String targetGroup,
      String description
  ) {
    this.mobileCode = mobileCode;
    this.category = category;
    this.planName = planName;
    this.monthlyFee = monthlyFee;
    this.contractFee = contractFee;
    this.dataAmount = dataAmount;
    this.qosSpeed = qosSpeed;
    this.voice = voice;
    this.sms = sms;
    this.sharingData = sharingData;
    this.benefits = benefits;
    this.targetGroup = targetGroup;
    this.description = description;
  }
}