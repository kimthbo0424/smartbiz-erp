package com.smartbiz.erp.payment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long> {

    List<PayrollDetail> findByPayrollIdOrderByDetailIdAsc(Long payrollId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from PayrollDetail d where d.payrollId = :payrollId")
    void deleteByPayrollId(@Param("payrollId") Long payrollId);

}
