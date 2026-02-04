package com.smartbiz.erp.dto.inventory;

import com.smartbiz.erp.entity.InventoryTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryMoveDto {

    private Long id;                 // 트랜잭션 ID (기존 transactionId → id 로 통일)
    private Long productId;
    private String productName;

    private Long warehouseId;        // 해당 트랜잭션의 창고 ID
    private String warehouseName;    // 창고 이름

    private InventoryTransactionType type;
    private int quantity;
    private String reason;
    private Long relatedClientId;
    private LocalDateTime occurredAt;
}
