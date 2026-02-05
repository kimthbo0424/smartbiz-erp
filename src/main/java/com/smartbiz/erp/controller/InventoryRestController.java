package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.dto.inventory.InventoryAdjustDto;
import com.smartbiz.erp.dto.inventory.InventoryCancelCheckResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryCancelRequestDto;
import com.smartbiz.erp.dto.inventory.InventoryMoveRequestDto;
import com.smartbiz.erp.dto.inventory.InventoryMoveResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryRequestDto;
import com.smartbiz.erp.dto.inventory.InventoryResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryTransactionResponseDto;
import com.smartbiz.erp.entity.InventoryTransaction;
import com.smartbiz.erp.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryRestController {

    private final InventoryService inventoryService;

    // 1. 재고 목록 조회 (창고 기준)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryResponseDto>>> getInventoryPage(
            @RequestParam("warehouseId") Long warehouseId,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getInventoryPageResponse(warehouseId, pageable)
                )
        );
    }

    // 2. 입고
    @PostMapping("/in")
    public ResponseEntity<ApiResponse<Void>> stockIn(
            @Valid @RequestBody InventoryRequestDto request
    ) {
        inventoryService.stockIn(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 3. 출고
    @PostMapping("/out")
    public ResponseEntity<ApiResponse<Void>> stockOut(
            @Valid @RequestBody InventoryRequestDto request
    ) {
        inventoryService.stockOut(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 4. 재고 이동
    @PostMapping("/move")
    public ResponseEntity<ApiResponse<Void>> moveStock(
            @Valid @RequestBody InventoryMoveRequestDto request
    ) {
        inventoryService.moveStock(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 5. 재고 조정
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @Valid @RequestBody InventoryAdjustDto request
    ) {
        inventoryService.adjustStock(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 6. 조정 이력 조회
    @GetMapping("/moves/adjust")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionResponseDto>>> getAdjustTransactionPage(
            @RequestParam("warehouseId") Long warehouseId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getAdjustTransactionPage(warehouseId, pageable)
                )
        );
    }

    // 7. 이동 이력 조회
    @GetMapping("/moves/transfer")
    public ResponseEntity<ApiResponse<Page<InventoryMoveResponseDto>>> getMoveTransactionPage(
            @RequestParam(name = "warehouseId", required = false) Long warehouseId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getMoveTransactionPage(warehouseId, pageable)
                )
        );
    }

    // 8. 취소
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTransaction(
            @Valid @RequestBody InventoryCancelRequestDto request
    ) {
        inventoryService.cancelTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    // 9. 입출고
    @GetMapping("/moves/inout")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionResponseDto>>> getInOutTransactions(
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getInOutTransactionPage(warehouseId, pageable)
                )
        );
    }

    // 10. 취소 가능 여부
    @GetMapping("/transactions/{id}/cancelable")
    public ResponseEntity<ApiResponse<InventoryCancelCheckResponseDto>> checkCancelable(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.checkCancelable(id)
                )
        );
    }

    // 11. 리포트
    @GetMapping("/moves/report")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getReportTransactions(
            @RequestParam("warehouseId") Long warehouseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getTransactionsForReport(warehouseId)
                )
        );
    }

    // 12. 안전재고 수정
    @PatchMapping("/safety-stock")
    public ResponseEntity<ApiResponse<Void>> updateSafetyStock(
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("productId") Long productId,
            @RequestParam("safetyStockQty") Integer safetyStockQty
    ) {
        inventoryService.updateSafetyStock(warehouseId, productId, safetyStockQty);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}