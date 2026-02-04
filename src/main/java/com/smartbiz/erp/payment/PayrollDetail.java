package com.smartbiz.erp.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_detail",
        uniqueConstraints = @UniqueConstraint(name = "uk_payroll_detail_once", columnNames = {"payroll_id", "item_id"}))
@Getter
@Setter
public class PayrollDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @Column(name = "payroll_id", nullable = false)
    private Long payrollId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name_snapshot", nullable = false, length = 100)
    private String itemNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type_snapshot", nullable = false, length = 20)
    private PayrollItem.ItemType itemTypeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "calc_type_snapshot", nullable = false, length = 20)
    private PayrollItem.CalcType calcTypeSnapshot;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "rate", precision = 7, scale = 4)
    private BigDecimal rate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
