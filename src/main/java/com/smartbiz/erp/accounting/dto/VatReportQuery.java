package com.smartbiz.erp.accounting.dto;

public class VatReportQuery {

    /**
     * yyyy-MM (예: 2026-01)
     */
    private String month;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
