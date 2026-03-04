package com.uplus.crm.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmployeeDetail {

	@Id
	@Column(name = "emp_id")
	private Integer empId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "emp_id")
	private Employee employee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dept_id", nullable = false)
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_role_id", nullable = false)
	private JobRole jobRole;

	@Column(name = "joined_at")
	private LocalDate joinedAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public void updateDetail(Department department, JobRole jobRole, LocalDate joinedAt) {
		this.department = department;
		this.jobRole = jobRole;
		this.joinedAt = joinedAt;
		this.updatedAt = LocalDateTime.now();
	}
}