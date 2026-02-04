package com.smartbiz.erp.dto.product_supplier;

import lombok.Getter;

@Getter
public class ProductSupplierUpdateRequestDto {

    private String supplierSku;
    private Integer leadTime;
    private Boolean isPrimary;
}
