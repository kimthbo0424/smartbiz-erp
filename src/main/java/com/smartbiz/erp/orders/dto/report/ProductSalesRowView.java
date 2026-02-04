package com.smartbiz.erp.orders.dto.report;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductSalesRowView {

    private final String productName;
    private final long totalAmount;

    // JPQL select new 로 BigDecimal 합계를 받아 long으로 변환
    public ProductSalesRowView(String productName, BigDecimal totalAmount) {
        this.productName = productName;
        if (totalAmount == null) this.totalAmount = 0L;
        else this.totalAmount = totalAmount.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public String getProductName() { return productName; }
    public long getTotalAmount() { return totalAmount; }
}
