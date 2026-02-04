package com.smartbiz.erp.dto.product;

import com.smartbiz.erp.entity.Product;
import com.smartbiz.erp.entity.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private ProductStatus status;
    private Boolean isActive;

    private Long categoryId;
    private String categoryName;

    public ProductResponseDto(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.sku = p.getSku();
        this.barcode = p.getBarcode();
        this.costPrice = p.getCostPrice();
        this.unitPrice = p.getUnitPrice();
        this.status = p.getStatus();
        this.isActive = p.getIsActive();

        if (p.getCategory() != null) {
            this.categoryId = p.getCategory().getId();
            this.categoryName = p.getCategory().getName();
        }
    }
}