package com.smartbiz.erp.dto.client;

import java.math.BigDecimal;

import com.smartbiz.erp.entity.enums.ClientType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientCreateDto {
    private String name;
    private ClientType type;
    private String bizNo;
    private String billingAddr;
    private String shippingAddr;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private Boolean isActive;
}
