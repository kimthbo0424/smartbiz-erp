package com.smartbiz.erp.employee.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, Long> {

    // 변경일 기준 최신순 정렬
    @Query("SELECT h FROM EmployeeHistory h ORDER BY h.changeDate DESC, h.historyId DESC")
    List<EmployeeHistory> findAllOrderByChangeDateDesc();
}
