package com.uplus.crm.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perm_id")
    private Integer permId;

    @Column(name = "perm_code", nullable = false, length = 100)
    private String permCode;

    @Column(name = "perm_desc", columnDefinition = "TEXT")
    private String permDesc;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
}