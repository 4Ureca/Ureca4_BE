package com.uplus.crm.domain.summary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 조건 정의 (테이블 18)
 * - 검색/필터 항목의 원본(필터키/대안코드/표시명)을 관리
 * - CRUD 없이 읽기 전용 (더미데이터로만 관리)
 */
@Entity
@Table(name = "filter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_id")
    private Integer filterId;

    @Column(name = "filter_key", nullable = false, unique = true, length = 50)
    private String filterKey;

    @Column(name = "filter_name", nullable = false, length = 50)
    private String filterName;
}