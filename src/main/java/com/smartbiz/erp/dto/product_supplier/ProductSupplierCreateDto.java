package com.smartbiz.erp.dto.product_supplier;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplierCreateDto {

    private Long productId;
    private Long clientId;      // 공급사(거래처) ID

    private String supplierSku;
    private Integer leadTime;
    private Boolean isPrimary;
}
