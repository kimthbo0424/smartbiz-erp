package com.smartbiz.erp.dto.product_supplier;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductSupplierCreateRequestDto {

    @NotNull
    private Long productId;

    @NotNull
    private Long supplierId;

    private String supplierSku;
    private Integer leadTime;
    private Boolean isPrimary;
}
