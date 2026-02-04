package com.smartbiz.erp.orders.dto;

public class OrderItemForm {
    private String productName;
    private int qty = 1;
    private long unitPrice = 0;

    public String getProductName() { return productName; }
    public int getQty() { return qty; }
    public long getUnitPrice() { return unitPrice; }

    public void setProductName(String productName) { this.productName = productName; }
    public void setQty(int qty) { this.qty = qty; }
    public void setUnitPrice(long unitPrice) { this.unitPrice = unitPrice; }
}
