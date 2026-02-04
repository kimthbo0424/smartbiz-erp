package com.smartbiz.erp.dto.inventory;

import com.smartbiz.erp.entity.InventoryTransaction;
import com.smartbiz.erp.entity.InventoryTransactionType;
import com.smartbiz.erp.repository.view.InventoryTransactionView;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryTransactionResponseDto {

    private Long id;
    private Long productId;
    private String productName;

    private Long warehouseId;
    private String warehouseName;

    private InventoryTransactionType type;
    private int quantity;
    private String reason;

    private Long relatedClientId;
    private LocalDateTime occurredAt;

    public static InventoryTransactionResponseDto from(
            InventoryTransaction tx,
            String productName,
            String warehouseName
    ) {
        return InventoryTransactionResponseDto.builder()
                .id(tx.getId())
                .productId(tx.getProductId())
                .productName(productName)
                .warehouseId(tx.getWarehouseId())
                .warehouseName(warehouseName)
                .type(tx.getType())
                .quantity(tx.getQuantity())
                .reason(tx.getReason())
                .relatedClientId(tx.getRelatedClientId())
                .occurredAt(tx.getOccurredAt())
                .build();
    }
    
    public static InventoryTransactionResponseDto from(
            InventoryTransactionView view
    ) {
        return InventoryTransactionResponseDto.builder()
                .id(view.getId())
                .productId(view.getProductId())
                .productName(view.getProductName())
                .warehouseId(view.getWarehouseId())
                .warehouseName(view.getWarehouseName())
                .quantity(view.getQuantity())
                .type(view.getType())
                .reason(view.getReason())
                .relatedClientId(view.getRelatedClientId())
                .occurredAt(view.getOccurredAt())
                .build();
    }
}
