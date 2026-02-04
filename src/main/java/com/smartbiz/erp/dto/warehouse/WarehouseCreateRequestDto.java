package com.smartbiz.erp.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class WarehouseCreateRequestDto {

    @NotBlank(message = "창고명은 필수입니다.")
    @Size(max = 100, message = "창고명은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "창고 코드는 필수입니다.")
    @Size(max = 50, message = "창고 코드는 50자를 초과할 수 없습니다.")
    private String code;

    @Size(max = 255, message = "위치는 255자를 초과할 수 없습니다.")
    private String location;

    @Size(max = 100, message = "담당자명은 100자를 초과할 수 없습니다.")
    private String manager;

    private Integer capacity;
}
