package com.smartbiz.erp.dto.inventory;

import com.smartbiz.erp.entity.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDto {

    private Long productId;
    private Long warehouseId;
    private int quantity;
    private int safetyStockQty;
    // 필요 시 프론트 표시용
    private String productName;

    public InventoryResponseDto(Inventory inventory) {
        this.productId = inventory.getProductId();
        this.warehouseId = inventory.getWarehouseId();
        this.quantity = inventory.getQuantity();
        this.safetyStockQty = inventory.getSafetyStockQty();
        this.productName = null;
    }
}
