package com.smartbiz.erp.orders.dto;

public class OrderSearchQuery {
    private String partnerName;
    private String managerName;
    private String status;    // PENDING/SHIPPED...
    private String orderDate; // yyyy-MM-dd  (템플릿에서 q.orderDate 쓰는 경우)

    public String getPartnerName() { return partnerName; }
    public String getManagerName() { return managerName; }
    public String getStatus() { return status; }
    public String getOrderDate() { return orderDate; }

    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setStatus(String status) { this.status = status; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}
