package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.dto.warehouse.WarehouseCreateRequestDto;
import com.smartbiz.erp.dto.warehouse.WarehouseResponseDto;
import com.smartbiz.erp.dto.warehouse.WarehouseUpdateRequestDto;
import com.smartbiz.erp.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warehouse")
public class WarehouseRestController {

    private final WarehouseService warehouseService;

    // 전체 조회
    @GetMapping
    public ApiResponse<List<WarehouseResponseDto>> getAll() {
        return ApiResponse.success(
                warehouseService.findAllResponse()
        );
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponseDto> getOne(
    		@PathVariable("id") Long id
    ) {
        return ApiResponse.success(
                warehouseService.findByIdResponse(id)
        );
    }

    // 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(
            @Valid @RequestBody WarehouseCreateRequestDto dto
    ) {
        warehouseService.create(dto);
        return ApiResponse.success(null, "창고가 등록되었습니다.");
    }

    // 수정 (부분 수정)
    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
    		@PathVariable("id") Long id,
            @RequestBody WarehouseUpdateRequestDto dto
    ) {
        warehouseService.update(id, dto);
        return ApiResponse.success(null, "창고 정보가 수정되었습니다.");
    }

    // 비활성화 (Soft Delete)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        warehouseService.deactivate(id);
    }
}