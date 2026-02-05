package com.smartbiz.erp.dto.inventory;

import com.smartbiz.erp.entity.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class InventoryResponseDto {

    private Long productId;
    private Long warehouseId;
    private int quantity;
    private int safetyStockQty;
    private String productName;

    public InventoryResponseDto(Inventory inventory) {
        this.productId = inventory.getProductId();
        this.warehouseId = inventory.getWarehouseId();
        this.quantity = inventory.getQuantity();
        this.safetyStockQty = inventory.getSafetyStockQty();
        this.productName = null;
    }

    public InventoryResponseDto(
            Long productId,
            Long warehouseId,
            int quantity,
            int safetyStockQty,
            String productName
    ) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.safetyStockQty = safetyStockQty;
        this.productName = productName;
    }
}
