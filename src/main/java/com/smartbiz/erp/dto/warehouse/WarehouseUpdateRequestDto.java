package com.smartbiz.erp.dto.warehouse;

import lombok.Getter;

@Getter
public class WarehouseUpdateRequestDto {

    private String name;
    private String code;
    private String location;
    private String manager;
    private Integer capacity;
    private Boolean isActive;
}