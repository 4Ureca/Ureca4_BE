package com.uplus.crm.domain.bookmark.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_bookmarks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "manual_id")
    private Integer manualId;

    @Column(name = "consult_id")
    private Long consultId;

    @Column(name = "best_practice_id")
    private Integer bestPracticeId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
