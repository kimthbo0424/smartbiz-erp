package com.smartbiz.erp.accounting.dto;

public class ProfitReportRowView {
    private final String label;       // 예: 담당자(또는 부서)
    private final long salesSubtotal;
    private final long grossProfit;
    private final double profitRate;

    public ProfitReportRowView(String label, long salesSubtotal, long grossProfit, double profitRate) {
        this.label = label;
        this.salesSubtotal = salesSubtotal;
        this.grossProfit = grossProfit;
        this.profitRate = profitRate;
    }

    public String getLabel() { return label; }
    public long getSalesSubtotal() { return salesSubtotal; }
    public long getGrossProfit() { return grossProfit; }
    public double getProfitRate() { return profitRate; }
}
