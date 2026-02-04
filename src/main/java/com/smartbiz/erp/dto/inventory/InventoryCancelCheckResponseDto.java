package com.smartbiz.erp.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryCancelCheckResponseDto {

    private boolean cancelable;
    private String reason; // 불가능 사유 (nullable)
}

