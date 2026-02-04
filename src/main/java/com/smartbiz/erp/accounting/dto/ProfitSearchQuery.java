package com.smartbiz.erp.accounting.dto;

public class ProfitSearchQuery {
    private String profitFrom; // yyyy-MM-dd
    private String profitTo;   // yyyy-MM-dd
    private String profitDept; // 부서(1차: 라벨만)

    public String getProfitFrom() { return profitFrom; }
    public String getProfitTo() { return profitTo; }
    public String getProfitDept() { return profitDept; }

    public void setProfitFrom(String profitFrom) { this.profitFrom = profitFrom; }
    public void setProfitTo(String profitTo) { this.profitTo = profitTo; }
    public void setProfitDept(String profitDept) { this.profitDept = profitDept; }
}
