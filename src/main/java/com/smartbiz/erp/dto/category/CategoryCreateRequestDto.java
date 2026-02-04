package com.smartbiz.erp.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequestDto(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String name,
        Long parentId
) {}
