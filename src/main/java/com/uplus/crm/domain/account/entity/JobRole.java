package com.uplus.crm.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JobRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_role_id")
    private Integer jobRoleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "role_desc", length = 200)
    private String roleDesc;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}