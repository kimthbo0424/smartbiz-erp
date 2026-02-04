package com.smartbiz.erp.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    List<PayrollItem> findByUseYnOrderByItemTypeAscItemIdAsc(String useYn);
}
