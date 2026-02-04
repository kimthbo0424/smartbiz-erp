package com.smartbiz.erp.dto.client;

import java.math.BigDecimal;
import com.smartbiz.erp.entity.enums.ClientType;

public record ClientResponseDto(
    Long id,
    String name,
    ClientType type,
    String bizNo,
    String billingAddr,
    String shippingAddr,
    BigDecimal creditLimit,
    String paymentTerms,
    Boolean active
) {}
