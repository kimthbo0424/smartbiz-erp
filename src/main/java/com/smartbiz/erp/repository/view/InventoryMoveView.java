package com.smartbiz.erp.repository.view;

import java.time.LocalDateTime;

public interface InventoryMoveView {

    Long getTransactionId();      // 기준 트랜잭션 (OUT 쪽)
    Long getProductId();
    String getProductName();

    Long getFromWarehouseId();
    String getFromWarehouseName();

    Long getToWarehouseId();
    String getToWarehouseName();

    Integer getQuantity();
    String getReason();
    Long getRelatedClientId();
    LocalDateTime getOccurredAt();
}
