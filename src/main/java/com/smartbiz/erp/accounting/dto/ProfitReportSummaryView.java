// src/main/java/com/smartbiz/erp/accounting/dto/ProfitReportSummaryView.java
package com.smartbiz.erp.accounting.dto;

public class ProfitReportSummaryView {
    private final long salesSubtotal;
    private final long salesVat;
    private final long salesTotal;
    private final long grossProfit;
    private final long cogs;
    private final double profitRate;
    private final long orderCount;

    public ProfitReportSummaryView(long salesSubtotal,
                                   long salesVat,
                                   long salesTotal,
                                   long grossProfit,
                                   long cogs,
                                   double profitRate,
                                   long orderCount) {
        this.salesSubtotal = salesSubtotal;
        this.salesVat = salesVat;
        this.salesTotal = salesTotal;
        this.grossProfit = grossProfit;
        this.cogs = cogs;
        this.profitRate = profitRate;
        this.orderCount = orderCount;
    }

    public long getSalesSubtotal() { return salesSubtotal; }
    public long getSalesVat() { return salesVat; }
    public long getSalesTotal() { return salesTotal; }
    public long getGrossProfit() { return grossProfit; }
    public long getCogs() { return cogs; }
    public double getProfitRate() { return profitRate; }
    public long getOrderCount() { return orderCount; }
}
