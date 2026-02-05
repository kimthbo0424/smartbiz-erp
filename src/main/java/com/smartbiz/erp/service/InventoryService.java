package com.smartbiz.erp.service;

import com.smartbiz.erp.dto.inventory.*;
import com.smartbiz.erp.entity.Inventory;
import com.smartbiz.erp.entity.InventoryTransaction;
import com.smartbiz.erp.entity.InventoryTransactionType;
import com.smartbiz.erp.repository.InventoryRepository;
import com.smartbiz.erp.repository.InventoryTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    // REST 표준 메서드 (공식 API 전용)
    // 재고 조회 (페이지)
    @Transactional(readOnly = true)
    public Page<InventoryResponseDto> getInventoryPageResponse(
            Long warehouseId,
            Pageable pageable
    ) {
        return inventoryRepository
        		.findInventoryWithProductName(warehouseId, pageable);
    }

    // 입고
    public void stockIn(InventoryRequestDto dto) {
        Inventory inventory = inventoryRepository
        		.findForUpdate(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException("재고가 존재하지 않습니다."));

        inventory.increase(dto.getQuantity());

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(dto.getProductId())
                        .warehouseId(dto.getWarehouseId())
                        .quantity(dto.getQuantity())
                        .type(InventoryTransactionType.IN)
                        .reason(dto.getReason())
                        .relatedClientId(dto.getRelatedClientId())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }

    // 출고
    public void stockOut(InventoryRequestDto dto) {
        Inventory inventory = inventoryRepository
        		.findForUpdate(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException("재고가 존재하지 않습니다."));

        if (inventory.getQuantity() < dto.getQuantity()) {
            throw new IllegalStateException("재고 수량이 부족합니다.");
        }

        inventory.decrease(dto.getQuantity());

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(dto.getProductId())
                        .warehouseId(dto.getWarehouseId())
                        .quantity(dto.getQuantity())
                        .type(InventoryTransactionType.OUT)
                        .reason(dto.getReason())
                        .relatedClientId(dto.getRelatedClientId())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 재고 이동(MOVE)
     *
     * MOVE는 DB상 단일 레코드가 아닌,
     * OUT + IN 두 개의 InventoryTransaction으로 표현된다.
     *
     * 두 트랜잭션은 다음 규칙으로 하나의 MOVE로 간주한다.
     * 1. reason 이 "[MOVE]" 로 시작
     * 2. 동일 productId
     * 3. 동일 quantity
     * 4. 같은 요청에서 생성됨
     *
     * DB 스키마 변경 없이 논리적 개념으로만 관리한다.
     */
    public void moveStock(InventoryMoveRequestDto dto) {

        Long fromId = dto.getFromWarehouseId();
        Long toId = dto.getToWarehouseId();

        if (fromId.equals(toId)) {
            return;
        }

        // 데드락 방지: 항상 작은 창고ID 먼저 잠금
        List<Long> lockOrder = (fromId < toId)
                ? List.of(fromId, toId)
                : List.of(toId, fromId);

        List<Inventory> locked = inventoryRepository
                .findForUpdateByProductAndWarehouses(dto.getProductId(), lockOrder);

        Inventory from = locked.stream()
                .filter(i -> i.getWarehouseId().equals(fromId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("출발 창고 재고가 없습니다."));

        Inventory to = locked.stream()
                .filter(i -> i.getWarehouseId().equals(toId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("도착 창고 재고가 없습니다."));

        if (from.getQuantity() < dto.getQuantity()) {
            throw new IllegalStateException("이동할 재고가 부족합니다.");
        }

        from.decrease(dto.getQuantity());
        to.increase(dto.getQuantity());

        // 옵션 2 유지: OUT + IN 2건 기록
        String reason = "[MOVE] " + dto.getReason();

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(dto.getProductId())
                        .warehouseId(fromId)
                        .quantity(dto.getQuantity())
                        .type(InventoryTransactionType.OUT)
                        .reason(reason)
                        .relatedClientId(dto.getRelatedClientId())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(dto.getProductId())
                        .warehouseId(toId)
                        .quantity(dto.getQuantity())
                        .type(InventoryTransactionType.IN)
                        .reason(reason)
                        .relatedClientId(dto.getRelatedClientId())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }
    
    // 취소
    @Transactional
    public void cancelTransaction(InventoryCancelRequestDto dto) {
    	
        InventoryTransaction origin = inventoryTransactionRepository
                .findById(dto.getTransactionId())
                .orElseThrow(() ->
                        new EntityNotFoundException("취소 대상 트랜잭션이 존재하지 않습니다.")
                );
        
        boolean hasLaterTransaction =
    	        inventoryTransactionRepository.existsByProductIdAndOccurredAtAfter(
    	                origin.getProductId(),
    	                origin.getOccurredAt()
    	        );

    	if (hasLaterTransaction) {
    	    throw new IllegalStateException(
    	            "이후 재고 변동이 존재하여 취소할 수 없습니다."
    	    );
    	}

        // 이미 취소된 트랜잭션 방지
        if (origin.getReason() != null && origin.getReason().startsWith("[CANCEL]")) {
            throw new IllegalStateException("이미 취소된 트랜잭션입니다.");
        }

        // MOVE 트랜잭션인 경우 (OUT + IN 쌍 처리)
        if (origin.getReason() != null && origin.getReason().startsWith("[MOVE]")) {
            cancelMoveGroup(origin, dto.getReason());
            return;
        }

        // 일반 IN / OUT / ADJUST 취소
        Inventory inventory = inventoryRepository
                .findForUpdate(origin.getProductId(), origin.getWarehouseId())
                .orElseThrow(() ->
                        new EntityNotFoundException("재고가 존재하지 않습니다.")
                );

        InventoryTransactionType cancelType;

        switch (origin.getType()) {
            case IN -> {
                inventory.decrease(origin.getQuantity());
                cancelType = InventoryTransactionType.OUT;
            }
            case OUT -> {
                inventory.increase(origin.getQuantity());
                cancelType = InventoryTransactionType.IN;
            }
            case ADJUST -> {
                // ADJUST는 수량을 반대로 되돌림
                inventory.adjust(-origin.getQuantity());
                cancelType = InventoryTransactionType.ADJUST;
            }
            default -> throw new IllegalStateException("취소할 수 없는 트랜잭션 타입입니다.");
        }

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(origin.getProductId())
                        .warehouseId(origin.getWarehouseId())
                        .quantity(origin.getQuantity())
                        .type(cancelType)
                        .reason("[CANCEL] " + dto.getReason())
                        .relatedClientId(origin.getRelatedClientId())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }

    private void cancelMoveGroup(InventoryTransaction base, String reason) {

        List<InventoryTransaction> moveGroup =
                inventoryTransactionRepository.findMoveGroup(
                        base.getProductId(),
                        base.getQuantity(),
                        base.getOccurredAt(),
                        base.getReason()
                );

        if (moveGroup.size() != 2) {
            throw new IllegalStateException("MOVE 트랜잭션 구성 오류");
        }

        for (InventoryTransaction tx : moveGroup) {

            Inventory inventory = inventoryRepository
                    .findForUpdate(tx.getProductId(), tx.getWarehouseId())
                    .orElseThrow(() ->
                            new EntityNotFoundException("재고가 존재하지 않습니다.")
                    );

            if (tx.getType() == InventoryTransactionType.OUT) {
                inventory.increase(tx.getQuantity());
            } else if (tx.getType() == InventoryTransactionType.IN) {
                inventory.decrease(tx.getQuantity());
            }

            inventoryTransactionRepository.save(
                    InventoryTransaction.builder()
                            .productId(tx.getProductId())
                            .warehouseId(tx.getWarehouseId())
                            .quantity(tx.getQuantity())
                            .type(tx.getType() == InventoryTransactionType.OUT
                                    ? InventoryTransactionType.IN
                                    : InventoryTransactionType.OUT
                            )
                            .reason("[CANCEL] MOVE 취소: " + reason)
                            .relatedClientId(tx.getRelatedClientId())
                            .occurredAt(LocalDateTime.now())
                            .build()
            );
        }
    }
    
    @Transactional(readOnly = true)
    public InventoryCancelCheckResponseDto checkCancelable(Long transactionId) {

        InventoryTransaction tx = inventoryTransactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new EntityNotFoundException("트랜잭션이 존재하지 않습니다.")
                );

        if (tx.getReason() != null && tx.getReason().startsWith("[CANCEL]")) {
            return new InventoryCancelCheckResponseDto(
                    false,
                    "이미 취소된 트랜잭션입니다."
            );
        }

        boolean hasLaterTransaction =
                inventoryTransactionRepository.existsByProductIdAndOccurredAtAfter(
                        tx.getProductId(),
                        tx.getOccurredAt()
                );

        if (hasLaterTransaction) {
            return new InventoryCancelCheckResponseDto(
                    false,
                    "이후 재고 변동이 존재합니다."
            );
        }

        return new InventoryCancelCheckResponseDto(true, null);
    }


    // 안전재고
    public void updateSafetyStock(
            Long warehouseId,
            Long productId,
            int safetyStockQty
    ) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() ->
                        new EntityNotFoundException("재고가 존재하지 않습니다.")
                );

        inventory.updateSafetyStockQty(safetyStockQty);
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponseDto> getAdjustTransactionPage(
            Long warehouseId,
            Pageable pageable
    ) {
        return inventoryTransactionRepository
                .findTransactionView(
                        InventoryTransactionType.ADJUST,
                        warehouseId,
                        pageable
                )
                .map(InventoryTransactionResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<InventoryMoveResponseDto> getMoveTransactionPage(
            Pageable pageable
    ) {
        return inventoryTransactionRepository
                .findMoveTransaction(pageable)
                .map(view -> InventoryMoveResponseDto.builder()
                        .transactionId(view.getTransactionId())
                        .productId(view.getProductId())
                        .productName(view.getProductName())
                        .fromWarehouseId(view.getFromWarehouseId())
                        .fromWarehouseName(view.getFromWarehouseName())
                        .toWarehouseId(view.getToWarehouseId())
                        .toWarehouseName(view.getToWarehouseName())
                        .quantity(view.getQuantity())
                        .reason(view.getReason())
                        .relatedClientId(view.getRelatedClientId())
                        .occurredAt(view.getOccurredAt())
                        .build()
                );
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryMoveResponseDto> getMoveTransactionPage(
            Long warehouseId,
            Pageable pageable
    ) {
        if (warehouseId == null) {
            return getMoveTransactionPage(pageable);
        }

        return inventoryTransactionRepository
                .findMoveTransactionByWarehouseId(warehouseId, pageable)
                .map(view -> InventoryMoveResponseDto.builder()
                        .transactionId(view.getTransactionId())
                        .productId(view.getProductId())
                        .productName(view.getProductName())
                        .fromWarehouseId(view.getFromWarehouseId())
                        .fromWarehouseName(view.getFromWarehouseName())
                        .toWarehouseId(view.getToWarehouseId())
                        .toWarehouseName(view.getToWarehouseName())
                        .quantity(view.getQuantity())
                        .reason(view.getReason())
                        .relatedClientId(view.getRelatedClientId())
                        .occurredAt(view.getOccurredAt())
                        .build()
                );
    }

    public void adjustStock(InventoryAdjustDto dto) {

        Inventory inventory = inventoryRepository
                .findForUpdate(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() ->
                        new EntityNotFoundException("재고가 존재하지 않습니다.")
                );

        inventory.adjust(dto.getAdjustQuantity());

        inventoryTransactionRepository.save(
                InventoryTransaction.builder()
                        .productId(dto.getProductId())
                        .warehouseId(dto.getWarehouseId())
                        .quantity(dto.getAdjustQuantity())
                        .type(InventoryTransactionType.ADJUST)
                        .reason(dto.getReason())
                        .occurredAt(LocalDateTime.now())
                        .build()
        );
    }
    
    // 입출고 내역
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponseDto> getInOutTransactionPage(
            Long warehouseId,
            Pageable pageable
    ) {
        return inventoryTransactionRepository
                .findInOutTransactionView(warehouseId, pageable)
                .map(InventoryTransactionResponseDto::from);
    }

    // 레거시 호환용 브릿지 메서드 (기존 코드 안 깨짐)
    /**
     * @deprecated 내부/MVC 호환용
     * REST API에서는 stockIn(dto) 사용
     */
    @Deprecated
    public InventoryTransaction addStock(
            Long productId,
            Long warehouseId,
            int quantity,
            String reason,
            Long relatedClientId
    ) {
        stockIn(new InventoryRequestDto(
                productId,
                warehouseId,
                quantity,
                reason,
                relatedClientId
        ));
        return null;
    }
    
    /**
     * @deprecated 내부/MVC 호환용
     */
    @Deprecated
    public InventoryTransaction reduceStock(
            Long productId,
            Long warehouseId,
            int quantity,
            String reason,
            Long relatedClientId
    ) {
        stockOut(new InventoryRequestDto(
                productId,
                warehouseId,
                quantity,
                reason,
                relatedClientId
        ));
        return null;
    }

    /**
     * @deprecated 내부/MVC 호환용
     */
    @Deprecated
    public void moveStock(
            Long productId,
            Long fromWarehouseId,
            Long toWarehouseId,
            int quantity,
            String reason,
            Long relatedClientId
    ) {
        moveStock(new InventoryMoveRequestDto(
                productId,
                fromWarehouseId,
                toWarehouseId,
                quantity,
                reason,
                relatedClientId
        ));
    }

    //기존 조회용 (내부/보고서)
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventory(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsForReport(Long warehouseId) {
        if (warehouseId == null) return List.of();
        return inventoryTransactionRepository.findByWarehouseId(warehouseId);
    }
}
