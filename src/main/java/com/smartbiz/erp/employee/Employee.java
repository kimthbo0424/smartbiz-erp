package com.smartbiz.erp.employee;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    @Column(name = "rank_id", nullable = false)
    private Long rankId;

    @Column(name = "title_id")
    private Long titleId;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "retire_date")
    private LocalDate retireDate;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(nullable = false, length = 13)
    private String jumin;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // 로그인 통합 컬럼들
    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // auth 는 int 로
    @Column(name = "auth", nullable = false)
    private int auth;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    public void touchLastLoginAt() {
        this.lastLoginAt = java.time.LocalDateTime.now();
    }
}
