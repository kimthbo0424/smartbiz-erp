package com.smartbiz.erp.dto.product_supplier;

import com.smartbiz.erp.entity.ProductSupplier;

import lombok.Getter;

@Getter
public class ProductSupplierResponseDto {

    private Long id;

    private Long productId;
    private String productName;

    private Long supplierId;
    private String supplierName;

    private String supplierSku;
    private Integer leadTime;
    private Boolean isPrimary;

    public ProductSupplierResponseDto(ProductSupplier ps) {
        this.id = ps.getId();

        this.productId = ps.getProduct().getId();
        this.productName = ps.getProduct().getName();

        this.supplierId = ps.getClient().getId();
        this.supplierName = ps.getClient().getName();

        this.supplierSku = ps.getSupplierSku();
        this.leadTime = ps.getLeadTime();
        this.isPrimary = ps.getIsPrimary();
    }
}
