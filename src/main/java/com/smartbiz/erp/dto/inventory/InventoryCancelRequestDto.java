package com.smartbiz.erp.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCancelRequestDto {

    @NotNull
    private Long transactionId;

    private String reason;
}
