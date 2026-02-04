package com.smartbiz.erp.orders.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderCreateForm {
    private String partnerName;
    private String managerName;
    private String managerPhone;
    private String orderDate; // yyyy-MM-dd

    // 화면에서 입력받는 할인(금액)
    private long discount = 0;

    // 화면에서 바인딩하는 합계(읽기/표시용, 저장 시에는 서버에서 재계산 권장)
    private long subtotal = 0;
    private long tax = 0;
    private long total = 0;

    private List<OrderItemForm> items = new ArrayList<>();

    public String getPartnerName() { return partnerName; }
    public String getManagerName() { return managerName; }
    public String getManagerPhone() { return managerPhone; }
    public String getOrderDate() { return orderDate; }
    public long getDiscount() { return discount; }

    public long getSubtotal() { return subtotal; }
    public long getTax() { return tax; }
    public long getTotal() { return total; }

    public List<OrderItemForm> getItems() { return items; }

    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setDiscount(long discount) { this.discount = discount; }

    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
    public void setTax(long tax) { this.tax = tax; }
    public void setTotal(long total) { this.total = total; }

    public void setItems(List<OrderItemForm> items) { this.items = items; }

    /**
     * 서버에서 금액 재계산 (저장/검증 시 반드시 호출 권장)
     * - 부가세 10% 단순 계산(필요하면 과세/면세 정책으로 확장)
     */
    public void recalcAmounts() {
        long sum = 0L;

        if (items != null) {
            for (OrderItemForm it : items) {
                if (it == null) continue;

                long qty = it.getQty();           // qty가 int여도 long으로 받기
                long unit = it.getUnitPrice();    // unitPrice 타입에 맞춰 long

                if (qty > 0 && unit > 0) {
                    sum += qty * unit;
                }
            }
        }

        this.subtotal = sum;
        this.tax = (long) Math.floor(sum * 0.1);
        this.total = this.subtotal + this.tax - this.discount;
        if (this.total < 0) this.total = 0;
    }

}
