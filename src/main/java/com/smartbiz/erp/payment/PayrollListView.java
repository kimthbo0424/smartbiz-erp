package com.smartbiz.erp.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollListView(
        Long payrollId,
        Long employeeId,
        String employeeName,
        Integer year,
        Integer month,
        LocalDate payDate,
        BigDecimal baseSalary,
        BigDecimal totalAllowance,
        BigDecimal totalDeduction,
        BigDecimal netPay,
        String status
) {
}
