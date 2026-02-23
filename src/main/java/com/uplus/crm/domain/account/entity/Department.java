package com.uplus.crm.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "dept_name", nullable = false, length = 30)
    private String deptName;

    @Column(name = "location", length = 225)
    private String location;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}