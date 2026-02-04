package com.smartbiz.erp.dto.product_supplier;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplierUpdateDto {

    private Long id;

    private String supplierSku;
    private Integer leadTime;
    private Boolean isPrimary;
}
