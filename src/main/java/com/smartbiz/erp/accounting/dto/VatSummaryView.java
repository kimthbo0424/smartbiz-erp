package com.smartbiz.erp.accounting.dto;

public class VatSummaryView {

    private final String monthLabel;
    private final long salesSubtotal;
    private final long salesVat;
    private final long salesTotal;
    private final int salesCount;

    // 1차: 매입 미구현이므로 0 처리
    private final long purchaseSubtotal;
    private final long purchaseVat;

    private final long payableVat;

    public VatSummaryView(String monthLabel,
                          long salesSubtotal,
                          long salesVat,
                          long salesTotal,
                          int salesCount,
                          long purchaseSubtotal,
                          long purchaseVat) {
        this.monthLabel = monthLabel;
        this.salesSubtotal = salesSubtotal;
        this.salesVat = salesVat;
        this.salesTotal = salesTotal;
        this.salesCount = salesCount;
        this.purchaseSubtotal = purchaseSubtotal;
        this.purchaseVat = purchaseVat;
        this.payableVat = salesVat - purchaseVat;
    }

    public String getMonthLabel() { return monthLabel; }
    public long getSalesSubtotal() { return salesSubtotal; }
    public long getSalesVat() { return salesVat; }
    public long getSalesTotal() { return salesTotal; }
    public int getSalesCount() { return salesCount; }
    public long getPurchaseSubtotal() { return purchaseSubtotal; }
    public long getPurchaseVat() { return purchaseVat; }
    public long getPayableVat() { return payableVat; }
}
