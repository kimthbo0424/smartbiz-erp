package com.smartbiz.erp.accounting.dto;

public class AccountingSummaryView {
    private final long monthSalesTotal;     // VAT 포함 매출(초기: SETTLED 주문 total 합)
    private final long monthPurchaseTotal;  // 초기 0
    private final long expectedVat;         // 초기: 매출 VAT 합(SETTLED 주문 tax 합)
    private final long unprocessedCount;    // 초기: 이번달 PENDING/SHIPPED 건수

    public AccountingSummaryView(long monthSalesTotal, long monthPurchaseTotal, long expectedVat, long unprocessedCount) {
        this.monthSalesTotal = monthSalesTotal;
        this.monthPurchaseTotal = monthPurchaseTotal;
        this.expectedVat = expectedVat;
        this.unprocessedCount = unprocessedCount;
    }

    public long getMonthSalesTotal() { return monthSalesTotal; }
    public long getMonthPurchaseTotal() { return monthPurchaseTotal; }
    public long getExpectedVat() { return expectedVat; }
    public long getUnprocessedCount() { return unprocessedCount; }
}
