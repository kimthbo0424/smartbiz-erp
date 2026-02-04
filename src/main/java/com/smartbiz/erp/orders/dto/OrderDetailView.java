package com.smartbiz.erp.orders.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailView {
    private Long id;
    private String orderNo;
    private String orderDate;
    private String status;

    private String partnerName;
    private String managerName;
    private String managerPhone;

    private long subtotal;
    private long tax;
    private long discount;
    private long total;

    private List<OrderItemLineView> items = new ArrayList<>();

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public String getPartnerName() { return partnerName; }
    public String getManagerName() { return managerName; }
    public String getManagerPhone() { return managerPhone; }
    public long getSubtotal() { return subtotal; }
    public long getTax() { return tax; }
    public long getDiscount() { return discount; }
    public long getTotal() { return total; }
    public List<OrderItemLineView> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
    public void setTax(long tax) { this.tax = tax; }
    public void setDiscount(long discount) { this.discount = discount; }
    public void setTotal(long total) { this.total = total; }
    public void setItems(List<OrderItemLineView> items) { this.items = items; }
}
