package com.smartbiz.erp.dto.inventory;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryMoveResponseDto {

    private Long transactionId;

    private Long productId;
    private String productName;

    private Long fromWarehouseId;
    private String fromWarehouseName;

    private Long toWarehouseId;
    private String toWarehouseName;

    private int quantity;
    private String reason;

    private Long relatedClientId;
    private LocalDateTime occurredAt;
}

