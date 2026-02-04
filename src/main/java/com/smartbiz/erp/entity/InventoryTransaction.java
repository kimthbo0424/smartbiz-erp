package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_transaction_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "from_warehouse_id")
    private Long fromWarehouseId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "trade_doc_id")
    private Long tradeDocId;

    // 🔥 핵심 수정 포인트
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InventoryTransactionType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "related_client_id")
    private Long relatedClientId;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
