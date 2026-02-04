package com.smartbiz.erp.dto.product;

import java.math.BigDecimal;

import com.smartbiz.erp.entity.enums.ProductStatus;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductCreateDto {
    private String name;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private ProductStatus status;
    private Long categoryId;
    private Boolean isActive;
}
