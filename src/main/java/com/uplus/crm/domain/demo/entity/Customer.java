package com.uplus.crm.domain.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * customers 테이블 매핑 (읽기 전용).
 * 추후 리팩터링 시 공유 엔티티로 이동 예정.
 */
@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "identification_num", nullable = false, length = 30)
    private String identificationNum;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "customer_type", nullable = false, columnDefinition = "ENUM('개인','법인')")
    private String customerType;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "grade_code", length = 20)
    private String gradeCode;

    @Column(name = "preferred_contact", columnDefinition = "ENUM('CALL','CHATTING')")
    private String preferredContact;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
}
