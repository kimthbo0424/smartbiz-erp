package com.smartbiz.erp.orders.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderEditForm extends OrderCreateForm {
    private Long id;
    private String orderNo;

    private long subtotal;
    private long tax;
    private long total;

    private List<OrderItemForm> items = new ArrayList<>();

    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public long getSubtotal() { return subtotal; }
    public long getTax() { return tax; }
    public long getTotal() { return total; }

    @Override
    public List<OrderItemForm> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
    public void setTax(long tax) { this.tax = tax; }
    public void setTotal(long total) { this.total = total; }
    @Override
    public void setItems(List<OrderItemForm> items) { this.items = items; }
}
