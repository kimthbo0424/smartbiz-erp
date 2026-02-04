package com.smartbiz.erp.dto.category;

import java.util.List;

public record CategoryResponseDto(
        Long id,
        String name,
        int level,
        boolean active,
        Long parentId
) {}
