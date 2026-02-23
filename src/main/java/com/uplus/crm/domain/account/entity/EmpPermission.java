package com.uplus.crm.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emp_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmpPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_perm_id")
    private Integer empPermId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perm_id", nullable = false)
    private Permission permission;  // ← 이 타입이 com.uplus.crm.domain.account.entity.Permission 이어야 해요

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
}