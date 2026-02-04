// src/main/java/com/smartbiz/erp/accounting/repository/ExpenseItemRepository.java
package com.smartbiz.erp.accounting.repository;

import com.smartbiz.erp.accounting.domain.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {
    Optional<ExpenseItem> findByName(String name);
}