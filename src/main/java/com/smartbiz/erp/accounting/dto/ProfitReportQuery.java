// src/main/java/com/smartbiz/erp/accounting/dto/ProfitReportQuery.java
package com.smartbiz.erp.accounting.dto;

public class ProfitReportQuery {
    // ✅ request param 충돌 방지용(손익 전용)
    private String profitFrom;   // yyyy-MM-dd
    private String profitTo;     // yyyy-MM-dd (inclusive로 받되 내부에서 toExclusive 처리)
    private String profitBasis;  // settled / shipped / all

    public String getProfitFrom() { return profitFrom; }
    public String getProfitTo() { return profitTo; }
    public String getProfitBasis() { return profitBasis; }

    public void setProfitFrom(String profitFrom) { this.profitFrom = profitFrom; }
    public void setProfitTo(String profitTo) { this.profitTo = profitTo; }
    public void setProfitBasis(String profitBasis) { this.profitBasis = profitBasis; }

    // ✅ 기존 서비스/로직 호환용(alias)
    public String getFrom() { return profitFrom; }
    public String getTo() { return profitTo; }
    public String getBasis() { return profitBasis; }

    public void setFrom(String from) { this.profitFrom = from; }
    public void setTo(String to) { this.profitTo = to; }
    public void setBasis(String basis) { this.profitBasis = basis; }
}
