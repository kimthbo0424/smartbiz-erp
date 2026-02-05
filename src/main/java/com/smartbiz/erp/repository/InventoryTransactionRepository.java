package com.smartbiz.erp.repository;

import com.smartbiz.erp.entity.InventoryTransaction;
import com.smartbiz.erp.entity.InventoryTransactionType;
import com.smartbiz.erp.repository.view.InventoryTransactionView;
import com.smartbiz.erp.repository.view.InventoryMoveView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    // 특정 창고의 모든 트랜잭션
    List<InventoryTransaction> findByWarehouseId(Long warehouseId);

    // 특정 상품의 모든 트랜잭션
    List<InventoryTransaction> findByProductId(Long productId);

    // 상품 + 창고
    List<InventoryTransaction> findByProductIdAndWarehouseId(
            Long productId,
            Long warehouseId
    );

    // 타입별 필터 (IN, OUT, ADJUST)
    List<InventoryTransaction> findByType(InventoryTransactionType type);

    // 날짜 범위 필터
    List<InventoryTransaction> findByOccurredAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 복합 필터
    List<InventoryTransaction> findByProductIdAndWarehouseIdAndType(
            Long productId,
            Long warehouseId,
            InventoryTransactionType type
    );

    // 페이징 전용

    // 입출고 이력 페이징 (창고 기준)
    Page<InventoryTransaction> findByWarehouseId(
            Long warehouseId,
            Pageable pageable
    );
    
    // 재고 이동(move)
    Page<InventoryTransaction> findByReasonStartingWith(
            String reasonPrefix,
            Pageable pageable
    );
    
    Page<InventoryTransaction> findByWarehouseIdIsNotNull(Pageable pageable);
    
    // ADJUST + 창고별 + 정렬/페이징
    Page<InventoryTransaction> findByWarehouseIdAndType(
            Long warehouseId,
            InventoryTransactionType type,
            Pageable pageable
    );

    // 전체 창고 ADJUST
    Page<InventoryTransaction> findByType(
            InventoryTransactionType type,
            Pageable pageable
    );
    
    // 취소
    boolean existsByReasonContaining(String keyword);
    
    boolean existsByProductIdAndOccurredAtAfter(
            Long productId,
            LocalDateTime occurredAt
    );
    
    // 쿼리 injection
    @Query("""
    	    select
				t.id as id,
				t.productId as productId,
				p.name as productName,
				t.warehouseId as warehouseId,
				w.name as warehouseName,
				t.quantity as quantity,
				t.type as type,
				t.reason as reason,
				t.relatedClientId as relatedClientId,
				t.occurredAt as occurredAt
    	    from InventoryTransaction t
    	    join Product p on p.id = t.productId
    	    join Warehouse w on w.id = t.warehouseId
    	    where t.type = :type
    	      and (:warehouseId is null or t.warehouseId = :warehouseId)
    	""")
    	Page<InventoryTransactionView> findTransactionView(
    	        @Param("type") InventoryTransactionType type,
    	        @Param("warehouseId") Long warehouseId,
    	        Pageable pageable
    	);
    
    @Query("""
    	    select
    	        t.id as id,
    	        t.productId as productId,
    	        p.name as productName,
    	        t.warehouseId as warehouseId,
    	        w.name as warehouseName,
    	        t.quantity as quantity,
    	        t.type as type,
    	        t.reason as reason,
    	        t.relatedClientId as relatedClientId,
    	        t.occurredAt as occurredAt
    	    from InventoryTransaction t
    	    join Product p on p.id = t.productId
    	    join Warehouse w on w.id = t.warehouseId
    	    where t.type = com.smartbiz.erp.entity.InventoryTransactionType.OUT
    	      and t.reason like '[MOVE]%'
    	""")
    	Page<com.smartbiz.erp.repository.view.InventoryTransactionView>
    	findMoveOutTransactionView(Pageable pageable);
    
    @Query("""
    	    select
    	        outTx.id as transactionId,
    	        outTx.productId as productId,
    	        p.name as productName,

    	        outTx.warehouseId as fromWarehouseId,
    	        fw.name as fromWarehouseName,

    	        inTx.warehouseId as toWarehouseId,
    	        tw.name as toWarehouseName,

    	        outTx.quantity as quantity,
    	        outTx.reason as reason,
    	        outTx.relatedClientId as relatedClientId,
    	        outTx.occurredAt as occurredAt
    	    from InventoryTransaction outTx
    	    join InventoryTransaction inTx
    	        on outTx.productId = inTx.productId
    	       and outTx.quantity = inTx.quantity
    	       and outTx.reason = inTx.reason
    	       and outTx.occurredAt = inTx.occurredAt
    	       and outTx.warehouseId <> inTx.warehouseId
    	    join Product p on p.id = outTx.productId
    	    join Warehouse fw on fw.id = outTx.warehouseId
    	    join Warehouse tw on tw.id = inTx.warehouseId
    	    where outTx.type = 'OUT'
    	      and inTx.type = 'IN'
    	      and outTx.reason like '[MOVE]%'
    	""")
    	Page<InventoryMoveView> findMoveTransaction(Pageable pageable);

    
    @Query("""
    	    select
    	        outTx.id as transactionId,
    	        outTx.productId as productId,
    	        p.name as productName,

    	        outTx.warehouseId as fromWarehouseId,
    	        fw.name as fromWarehouseName,

    	        inTx.warehouseId as toWarehouseId,
    	        tw.name as toWarehouseName,

    	        outTx.quantity as quantity,
    	        outTx.reason as reason,
    	        outTx.relatedClientId as relatedClientId,
    	        outTx.occurredAt as occurredAt
    	    from InventoryTransaction outTx
    	    join InventoryTransaction inTx
    	        on outTx.productId = inTx.productId
    	       and outTx.quantity = inTx.quantity
    	       and outTx.reason = inTx.reason
    	       and outTx.occurredAt = inTx.occurredAt
    	       and outTx.warehouseId <> inTx.warehouseId
    	    join Product p on p.id = outTx.productId
    	    join Warehouse fw on fw.id = outTx.warehouseId
    	    join Warehouse tw on tw.id = inTx.warehouseId
    	    where outTx.type = 'OUT'
    	      and inTx.type = 'IN'
    	      and outTx.reason like '[MOVE]%'
    	      and (
    	           :warehouseId is null
    	           or outTx.warehouseId = :warehouseId
    	           or inTx.warehouseId = :warehouseId
    	      )
    	""")
    	Page<InventoryMoveView> findMoveTransactionByWarehouseId(
    	        @Param("warehouseId") Long warehouseId,
    	        Pageable pageable
    	);
    
    @Query("""
    	    select t
    	    from InventoryTransaction t
    	    where t.productId = :productId
    	      and t.quantity = :quantity
    	      and t.reason = :reason
    	      and t.occurredAt = :occurredAt
    	""")
    	List<InventoryTransaction> findMoveGroup(
    	        @Param("productId") Long productId,
    	        @Param("quantity") int quantity,
    	        @Param("occurredAt") LocalDateTime occurredAt,
    	        @Param("reason") String reason
    	);

    @Query("""
    	    select count(t) > 0
    	    from InventoryTransaction t
    	    where t.productId = :productId
    	      and t.occurredAt > :occurredAt
    	""")
    	boolean existsLaterTransaction(
    	        @Param("productId") Long productId,
    	        @Param("occurredAt") LocalDateTime occurredAt
    	);

    @Query("""
    	    select
    	        t.id as id,
    	        t.productId as productId,
    	        p.name as productName,
    	        t.warehouseId as warehouseId,
    	        w.name as warehouseName,
    	        t.quantity as quantity,
    	        t.type as type,
    	        t.reason as reason,
    	        t.relatedClientId as relatedClientId,
    	        t.occurredAt as occurredAt
    	    from InventoryTransaction t
    	    join Product p on p.id = t.productId
    	    join Warehouse w on w.id = t.warehouseId
    	    where t.type in (com.smartbiz.erp.entity.InventoryTransactionType.IN,
    	                     com.smartbiz.erp.entity.InventoryTransactionType.OUT)
    	      and t.warehouseId = :warehouseId
    	    order by t.occurredAt desc
    	""")
    	Page<InventoryTransactionView> findInOutTransactionView(
    	    @Param("warehouseId") Long warehouseId,
    	    Pageable pageable
    	); 
}
