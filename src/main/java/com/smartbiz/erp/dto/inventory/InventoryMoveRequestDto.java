package com.smartbiz.erp.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMoveRequestDto {

    @NotNull
    private Long productId;

    @NotNull
    private Long fromWarehouseId;

    @NotNull
    private Long toWarehouseId;

    @Min(1)
    private int quantity;

    private String reason;

    private Long relatedClientId;
}