package com.smartbiz.erp.dto.category;

public record CategoryUpdateRequestDto(
	    String name,
	    Long parentId,
	    Boolean isActive
	) {}

