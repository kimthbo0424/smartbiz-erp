package com.smartbiz.erp.dto.client;

import java.math.BigDecimal;

import com.smartbiz.erp.entity.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientCreateRequestDto(

    @NotBlank
    String name,

    @NotNull
    ClientType type,

    String bizNo,
    String billingAddr,
    String shippingAddr,

    BigDecimal creditLimit,
    String paymentTerms,

    Boolean isActive
) {}
