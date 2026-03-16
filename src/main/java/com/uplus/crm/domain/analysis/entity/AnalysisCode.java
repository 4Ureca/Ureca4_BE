package com.uplus.crm.domain.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import jakarta.persistence.Id;

@Entity
@Table(name = "analysis_code")
@Getter
public class AnalysisCode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code_name")
  private String codeName;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "classification", columnDefinition = "ENUM('complaint_category', 'defense_category')")
  private String classification;
  private String description;
}