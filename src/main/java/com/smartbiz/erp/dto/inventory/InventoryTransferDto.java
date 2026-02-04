package com.smartbiz.erp.dto.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryTransferDto {

    private Long productId;      // 상품 ID
    private Long fromWarehouseId; // 출발 창고
    private Long toWarehouseId;   // 도착 창고
    private int quantity;        // 이동 수량
    private String reason;       // 이동 사유
}
