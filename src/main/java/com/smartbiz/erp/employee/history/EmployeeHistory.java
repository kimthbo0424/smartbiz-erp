package com.smartbiz.erp.employee.history;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "change_type", nullable = false, length = 30)
    private String changeType;

    @Column(name = "change_date", nullable = false)
    private LocalDate changeDate;

    @Column(name = "before_dept_id")
    private Long beforeDeptId;

    @Column(name = "after_dept_id")
    private Long afterDeptId;

    @Column(name = "before_position_id")
    private Long beforePositionId;

    @Column(name = "after_position_id")
    private Long afterPositionId;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
