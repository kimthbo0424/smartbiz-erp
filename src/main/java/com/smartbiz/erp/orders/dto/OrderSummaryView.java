package com.smartbiz.erp.orders.dto;

public class OrderSummaryView {
    private final Long id;
    private final String orderNo;
    private final String partnerName;
    private final String managerName;
    private final String managerPhone;
    private final String orderDate; // yyyy-MM-dd
    private final String status;
    private final long subtotal;
    private final long tax;
    private final long discount;
    private final long total;
    private final int profitRate; // 임시

    public OrderSummaryView(Long id, String orderNo, String partnerName, String managerName, String managerPhone,
                            String orderDate, String status, long subtotal, long tax, long discount, long total, int profitRate) {
        this.id = id;
        this.orderNo = orderNo;
        this.partnerName = partnerName;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.orderDate = orderDate;
        this.status = status;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.total = total;
        this.profitRate = profitRate;
    }

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public String getPartnerName() { return partnerName; }
    public String getManagerName() { return managerName; }
    public String getManagerPhone() { return managerPhone; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public long getSubtotal() { return subtotal; }
    public long getTax() { return tax; }
    public long getDiscount() { return discount; }
    public long getTotal() { return total; }
    public int getProfitRate() { return profitRate; }
}
