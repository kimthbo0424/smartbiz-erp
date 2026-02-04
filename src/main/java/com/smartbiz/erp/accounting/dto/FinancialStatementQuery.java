package com.smartbiz.erp.accounting.dto;

import java.time.LocalDate;

public class FinancialStatementQuery {

    private LocalDate fsAsOf; // 대차 기준일
    private LocalDate fsFrom; // 손익 from
    private LocalDate fsTo;   // 손익 to

    public LocalDate getFsAsOf() {
        return fsAsOf;
    }

    public void setFsAsOf(LocalDate fsAsOf) {
        this.fsAsOf = fsAsOf;
    }

    public LocalDate getFsFrom() {
        return fsFrom;
    }

    public void setFsFrom(LocalDate fsFrom) {
        this.fsFrom = fsFrom;
    }

    public LocalDate getFsTo() {
        return fsTo;
    }

    public void setFsTo(LocalDate fsTo) {
        this.fsTo = fsTo;
    }

    // ------------------------------------------------------------
    // ✅ AccountingController/기존 코드 호환용 alias
    // ------------------------------------------------------------

    /** 손익 From(호환) */
    public LocalDate getFrom() {
        return fsFrom;
    }

    public void setFrom(LocalDate from) {
        this.fsFrom = from;
    }

    /** 손익 To(호환) */
    public LocalDate getTo() {
        return fsTo;
    }

    public void setTo(LocalDate to) {
        this.fsTo = to;
    }

    /** 대차 기준일(호환) */
    public LocalDate getAsOf() {
        return fsAsOf;
    }

    public void setAsOf(LocalDate asOf) {
        this.fsAsOf = asOf;
    }
}
