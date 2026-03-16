package com.uplus.crm.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "analysis_code",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_analysis_code_full",
            columnNames = {"code_name", "classification"}
        )
    }
)
@Getter
@NoArgsConstructor
public class AnalysisCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code_name", length = 20, nullable = false)
  private String codeName;

  @Column(name = "display_name", length = 30)
  private String displayName;

  @Column(
      name = "classification",
      nullable = false,
      columnDefinition = "enum('complaint_category','defense_category','outbound_category')"
  )
  private String classification;

  @Column(name = "description", columnDefinition = "text")
  private String description;
}
