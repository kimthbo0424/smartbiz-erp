package com.smartbiz.erp.orders.dto;

public class OrderItemLineView {
    private final String productName;
    private final int qty;
    private final long unitPrice;
    private final long amount;

    public OrderItemLineView(String productName, int qty, long unitPrice, long amount) {
        this.productName = productName;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    public String getProductName() { return productName; }
    public int getQty() { return qty; }
    public long getUnitPrice() { return unitPrice; }
    public long getAmount() { return amount; }
}
