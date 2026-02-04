package com.smartbiz.erp.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollDetailRepository payrollDetailRepository;
    private final PayrollItemService payrollItemService;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<Payroll> findAll() {
        return payrollRepository.findAllByOrderByYearDescMonthDescPayrollIdDesc();
    }

    @Transactional(readOnly = true)
    public Payroll findById(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("payroll not found"));
    }

    @Transactional(readOnly = true)
    public List<PayrollDetail> findDetails(Long payrollId) {
        return payrollDetailRepository.findByPayrollIdOrderByDetailIdAsc(payrollId);
    }

    @Transactional(readOnly = true)
    public List<PayrollItem> getActiveItems() {
        return payrollItemService.findActiveItems();
    }
    
    public void updatePayrollItems(Long payrollId, List<Long> itemIds) {
        Payroll p = findById(payrollId);

        if (p.getStatus() == Payroll.Status.CONFIRMED) {
            throw new IllegalStateException("확정된 급여는 수정할 수 없습니다.");
        }

        // null 방어 + 중복 제거
        List<Long> ids = (itemIds == null) ? List.of() : itemIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        // 1) 기존 상세 전부 삭제(중복 방지 핵심)
        payrollDetailRepository.deleteByPayrollId(payrollId);

        BigDecimal base = nvl(p.getBaseSalary());
        BigDecimal totalAllowance = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;

        // 2) 선택된 항목만 다시 생성
        for (Long itemId : ids) {
            PayrollItem item = payrollItemService.findById(itemId);

            BigDecimal amount;
            BigDecimal rate = item.getRate();

            if (item.getCalcType() == PayrollItem.CalcType.FIXED) {
                amount = nvl(item.getAmount());
                rate = null;
            } else {
                BigDecimal r = nvl(item.getRate());
                amount = base.multiply(r).setScale(2, java.math.RoundingMode.HALF_UP);
            }

            PayrollDetail d = new PayrollDetail();
            d.setPayrollId(payrollId);
            d.setItemId(item.getItemId());
            d.setItemNameSnapshot(item.getItemName());
            d.setItemTypeSnapshot(item.getItemType());
            d.setCalcTypeSnapshot(item.getCalcType());
            d.setAmount(amount);
            d.setRate(rate);

            payrollDetailRepository.save(d);

            if (item.getItemType() == PayrollItem.ItemType.ALLOWANCE) {
                totalAllowance = totalAllowance.add(amount);
            } else {
                totalDeduction = totalDeduction.add(amount);
            }
        }

        BigDecimal netPay = base.add(totalAllowance).subtract(totalDeduction);

        p.setTotalAllowance(totalAllowance);
        p.setTotalDeduction(totalDeduction);
        p.setNetPay(netPay);

        // 수정 저장하면 최소 CALCULATED 상태로 보는게 자연스러움(원하면 유지해도 됨)
        if (p.getStatus() == Payroll.Status.UNCALC) {
            p.setStatus(Payroll.Status.CALCULATED);
        }

        payrollRepository.save(p);
    }
    
    public Payroll createPayroll(Long employeeId, Integer year, Integer month) {
        if (payrollRepository.findByEmployeeIdAndYearAndMonth(employeeId, year, month).isPresent()) {
            throw new IllegalArgumentException("payroll already exists for yyyymm");
        }

        Employee emp = employeeService.findById(employeeId);

        Payroll p = new Payroll();
        p.setEmployeeId(employeeId);
        p.setYear(year);
        p.setMonth(month);
        p.setBaseSalary(emp.getBaseSalary());
        p.setStatus(Payroll.Status.UNCALC);
        p.setTotalAllowance(BigDecimal.ZERO);
        p.setTotalDeduction(BigDecimal.ZERO);
        p.setNetPay(BigDecimal.ZERO);

        return payrollRepository.save(p);
    }

    public void calculate(Long payrollId) {
        Payroll p = findById(payrollId);

        if (p.getStatus() != Payroll.Status.UNCALC) {
            return;
        }

        payrollDetailRepository.deleteByPayrollId(payrollId);

        BigDecimal base = nvl(p.getBaseSalary());
        BigDecimal totalAllowance = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;

        List<PayrollItem> items = payrollItemService.findActiveItems();
        for (PayrollItem item : items) {

            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal rate = item.getRate();

            if (item.getCalcType() == PayrollItem.CalcType.FIXED) {
                amount = nvl(item.getAmount());
                rate = null;
            } else {
                BigDecimal r = nvl(item.getRate());
                amount = base.multiply(r).setScale(2, RoundingMode.HALF_UP);
            }

            PayrollDetail d = new PayrollDetail();
            d.setPayrollId(payrollId);
            d.setItemId(item.getItemId());

            d.setItemNameSnapshot(item.getItemName());
            d.setItemTypeSnapshot(item.getItemType());
            d.setCalcTypeSnapshot(item.getCalcType());

            d.setAmount(amount);
            d.setRate(rate);

            payrollDetailRepository.save(d);

            if (item.getItemType() == PayrollItem.ItemType.ALLOWANCE) {
                totalAllowance = totalAllowance.add(amount);
            } else {
                totalDeduction = totalDeduction.add(amount);
            }
        }

        BigDecimal netPay = base.add(totalAllowance).subtract(totalDeduction);

        p.setTotalAllowance(totalAllowance);
        p.setTotalDeduction(totalDeduction);
        p.setNetPay(netPay);
        p.setStatus(Payroll.Status.CALCULATED);

        payrollRepository.save(p);
    }

    public void confirm(Long payrollId) {
        Payroll p = findById(payrollId);

        if (p.getStatus() != Payroll.Status.CALCULATED) {
            return;
        }

        if (p.getPayDate() == null) {
            p.setPayDate(LocalDate.now());
        }
        p.setStatus(Payroll.Status.CONFIRMED);
        payrollRepository.save(p);
    }

    @Transactional(readOnly = true)
    public String getEmployeeName(Long employeeId) {
        Employee emp = employeeService.findById(employeeId);
        return emp.getName();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
