package com.smartbiz.erp.dto.warehouse;

import com.smartbiz.erp.entity.Warehouse;
import lombok.Getter;

@Getter
public class WarehouseResponseDto {

    private Long id;
    private String name;
    private String code;
    private String location;
    private String manager;
    private Integer capacity;
    private Boolean isActive;

    public WarehouseResponseDto(Warehouse warehouse) {
        this.id = warehouse.getId();
        this.name = warehouse.getName();
        this.code = warehouse.getCode();
        this.location = warehouse.getLocation();
        this.manager = warehouse.getManager();
        this.capacity = warehouse.getCapacity();
        this.isActive = warehouse.getIsActive();
    }
}