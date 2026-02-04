package com.smartbiz.erp.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustDto {

    @NotNull
    private Long productId;

    @NotNull
    private Long warehouseId;

    // 음수 가능 (감소)
    private int adjustQuantity;

    private String reason;
}