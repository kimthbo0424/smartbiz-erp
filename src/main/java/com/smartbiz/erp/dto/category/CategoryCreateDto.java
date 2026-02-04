package com.smartbiz.erp.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryCreateDto {
    private String name;
    private Integer level;
    private Long parentId;
    private Boolean isActive;
}
