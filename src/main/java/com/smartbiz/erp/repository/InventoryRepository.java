package com.smartbiz.erp.repository;

import com.smartbiz.erp.entity.Inventory;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // 특정 상품 + 창고
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // 특정 창고의 전체 재고 조회
    List<Inventory> findByWarehouseId(Long warehouseId);

    // 정렬 전용 조회
    List<Inventory> findByWarehouseId(Long warehouseId, Sort sort);

    // 특정 상품의 여러 창고 조회
    List<Inventory> findByProductId(Long productId);
    
    //페이징 처리
    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from Inventory i
        where i.productId = :productId
          and i.warehouseId = :warehouseId
    """)
    Optional<Inventory> findForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    // move에서 두 창고를 한 번에 잠그고 싶다면(선택)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from Inventory i
        where i.productId = :productId
          and i.warehouseId in (:warehouseIds)
    """)
    java.util.List<Inventory> findForUpdateByProductAndWarehouses(
            @Param("productId") Long productId,
            @Param("warehouseIds") java.util.List<Long> warehouseIds
    );
}
