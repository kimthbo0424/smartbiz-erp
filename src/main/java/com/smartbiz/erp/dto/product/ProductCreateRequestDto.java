package com.smartbiz.erp.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

import com.smartbiz.erp.entity.enums.ProductStatus;

@Getter
public class ProductCreateRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String sku;

    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;

    private ProductStatus status;
    
    @NotNull
    private Long categoryId;
}
