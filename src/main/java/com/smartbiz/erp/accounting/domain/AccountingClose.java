package com.smartbiz.erp.accounting.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_close")
public class AccountingClose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "close_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingCloseType type;

    /**
     * 예: MONTH=2026-01, QUARTER=2026-Q1, YEAR=2026
     */
    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    @Column(name = "closed_to", nullable = false)
    private LocalDate closedTo;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingCloseStatus status;

    protected AccountingClose() {
    }

    public AccountingClose(AccountingCloseType type, String periodKey, LocalDate closedTo, String closedBy) {
        this.type = type;
        this.periodKey = periodKey;
        this.closedTo = closedTo;
        this.closedAt = LocalDateTime.now();
        this.closedBy = closedBy;
        this.status = AccountingCloseStatus.CLOSED;
    }

    public void reverse(String reversedBy) {
        this.status = AccountingCloseStatus.REVERSED;
        this.closedBy = reversedBy;
        this.closedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AccountingCloseType getType() { return type; }
    public String getPeriodKey() { return periodKey; }
    public LocalDate getClosedTo() { return closedTo; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public String getClosedBy() { return closedBy; }
    public AccountingCloseStatus getStatus() { return status; }
}
