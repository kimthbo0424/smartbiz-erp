package com.smartbiz.erp.accounting.dto;

import java.util.List;

public class ProfitReportView {
    private final String from;            // 표시용 yyyy-MM-dd
    private final String to;              // 표시용 yyyy-MM-dd (inclusive)
    private final long salesSubtotal;     // 공급가액 매출
    private final long salesVat;          // VAT
    private final long discountTotal;     // 할인
    private final long salesTotal;        // 총액(공급+VAT-할인) = orders.total_amount 합
    private final long costTotal;         // 비용(근사)
    private final long operatingProfit;   // 영업이익(orders.profit_amount 합)
    private final long settledOrderCount; // 정산완료 건수
    private final String profitRate;      // "15.2" (%)
    private final List<ProfitDepartmentRowView> deptRows;

    public ProfitReportView(
            String from,
            String to,
            long salesSubtotal,
            long salesVat,
            long discountTotal,
            long salesTotal,
            long costTotal,
            long operatingProfit,
            long settledOrderCount,
            String profitRate,
            List<ProfitDepartmentRowView> deptRows
    ) {
        this.from = from;
        this.to = to;
        this.salesSubtotal = salesSubtotal;
        this.salesVat = salesVat;
        this.discountTotal = discountTotal;
        this.salesTotal = salesTotal;
        this.costTotal = costTotal;
        this.operatingProfit = operatingProfit;
        this.settledOrderCount = settledOrderCount;
        this.profitRate = profitRate;
        this.deptRows = deptRows;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public long getSalesSubtotal() { return salesSubtotal; }
    public long getSalesVat() { return salesVat; }
    public long getDiscountTotal() { return discountTotal; }
    public long getSalesTotal() { return salesTotal; }
    public long getCostTotal() { return costTotal; }
    public long getOperatingProfit() { return operatingProfit; }
    public long getSettledOrderCount() { return settledOrderCount; }
    public String getProfitRate() { return profitRate; }
    public List<ProfitDepartmentRowView> getDeptRows() { return deptRows; }
}
