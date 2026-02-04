package com.smartbiz.erp.repository;

import com.smartbiz.erp.entity.ProductSupplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, Long> {

    // 특정 상품의 공급사 목록
    List<ProductSupplier> findByProduct_Id(Long productId);

    // 특정 공급사(거래처)가 담당하는 상품들
    List<ProductSupplier> findByClient_Id(Long clientId);
}
