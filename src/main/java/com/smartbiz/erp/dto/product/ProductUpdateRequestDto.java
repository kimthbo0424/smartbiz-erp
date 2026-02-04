package com.smartbiz.erp.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartbiz.erp.entity.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequestDto {

    private String name;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private ProductStatus status;
    
    @JsonProperty("isActive")
    private Boolean isActive;

    private Long categoryId;
}
