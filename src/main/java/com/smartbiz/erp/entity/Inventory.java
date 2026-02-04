package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "safety_stock_qty", nullable = false)
    private Integer safetyStockQty;

    // 비즈니스 매서드

    // 재고 증가
    public void increase(int qty) {
        this.quantity += qty;
    }

    // 재고 감소
    public void decrease(int qty) {
        this.quantity -= qty;
    }

    // 절대값으로 재고 조정
    public void adjust(int newQty) {
        this.quantity = newQty;
    }
    
    // 안전 재고
    public void updateSafetyStockQty(int qty) {
        if (qty < 0) {
            throw new IllegalArgumentException("안전재고는 0 이상이어야 합니다.");
        }
        this.safetyStockQty = qty;
    }
}
