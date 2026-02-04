package com.smartbiz.erp.repository.view;

import com.smartbiz.erp.entity.InventoryTransactionType;
import java.time.LocalDateTime;

public interface InventoryTransactionView {

    Long getId();

    Long getProductId();
    String getProductName();

    Long getWarehouseId();
    String getWarehouseName();

    Integer getQuantity();

    InventoryTransactionType getType();

    String getReason();

    Long getRelatedClientId();

    LocalDateTime getOccurredAt();
}
