package com.smartbiz.erp.payment;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @GetMapping("/payroll")
    public String list(
            @RequestParam(name = "searchField", required = false, defaultValue = "name") String searchField,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "yearMonth", required = false) String yearMonth,
            @RequestParam(name = "status", required = false) String status,
            Model model
    ) {

        List<Payroll> payrolls = payrollService.findAll();

        String sf = (searchField == null || searchField.isBlank()) ? "name" : searchField.trim();
        String kw = (keyword == null) ? "" : keyword.trim();
        String ym = (yearMonth == null) ? "" : yearMonth.trim();
        String st = (status == null) ? "" : status.trim();

        // yearMonth 파싱
        Integer filterYear = null;
        Integer filterMonth = null;
        if (!ym.isEmpty()) {
            try {
                java.time.YearMonth parsed = java.time.YearMonth.parse(ym);
                filterYear = parsed.getYear();
                filterMonth = parsed.getMonthValue();
            } catch (Exception e) {
                // 잘못된 값이면 전체로 처리
                filterYear = null;
                filterMonth = null;
            }
        }

        // status 파싱
        Payroll.Status filterStatus = null;
        if (!st.isEmpty()) {
            try {
                filterStatus = Payroll.Status.valueOf(st);
            } catch (Exception e) {
                filterStatus = null;
            }
        }
        
        final Integer filterYearFinal = filterYear;
        final Integer filterMonthFinal = filterMonth;
        final Payroll.Status filterStatusFinal = filterStatus;

        final String sfFinal = sf;
        final String kwFinal = kw;

        // 필터링 (기존 방식 유지: 메모리에서 처리)
        List<Payroll> filtered = payrolls.stream()
                .filter(p -> {
                	if (filterYearFinal != null && !filterYearFinal.equals(p.getYear())) return false;
                	if (filterMonthFinal != null && !filterMonthFinal.equals(p.getMonth())) return false;
                	if (filterStatusFinal != null && p.getStatus() != filterStatusFinal) return false;

                    if (kwFinal.isEmpty()) return true;

                    if ("id".equalsIgnoreCase(sfFinal)) {
                        return p.getEmployeeId() != null && String.valueOf(p.getEmployeeId()).contains(kwFinal);
                    } else {
                        String employeeName = payrollService.getEmployeeName(p.getEmployeeId());
                        return employeeName != null && employeeName.contains(kwFinal);
                    }
                })
                .toList();


        List<PayrollListView> list = filtered.stream()
                .map(p -> new PayrollListView(
                        p.getPayrollId(),
                        p.getEmployeeId(),
                        payrollService.getEmployeeName(p.getEmployeeId()),
                        p.getYear(),
                        p.getMonth(),
                        p.getPayDate(),
                        p.getBaseSalary(),
                        p.getTotalAllowance(),
                        p.getTotalDeduction(),
                        p.getNetPay(),
                        p.getStatus().name()
                ))
                .toList();

        model.addAttribute("payrolls", list);

        // year months 는 기존 코드 유지해도 되지만 이제 UI 에서 안 쓰면 없어도 됨
        List<Integer> years = IntStream.rangeClosed(2020, 2035).boxed().toList();
        List<Integer> months = IntStream.rangeClosed(1, 12).boxed().toList();
        model.addAttribute("years", years);
        model.addAttribute("months", months);

        return "payment/payroll";
    }

    @GetMapping("/payroll/form")
    public String form(Model model) {

        List<Employee> employees = employeeService.findAll();

        List<Integer> years = IntStream.rangeClosed(2020, 2035).boxed().toList();
        List<Integer> months = IntStream.rangeClosed(1, 12).boxed().toList();

        model.addAttribute("employees", employees);
        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("items", payrollService.getActiveItems());

        return "payment/payroll-form";
    }

    @PostMapping("/payroll/save")
    public String save(@RequestParam("employeeId") Long employeeId,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam(name = "itemIds", required = false) List<Long> itemIds) {

    	Payroll p = payrollService.createPayroll(employeeId, year, month);
    	payrollService.updatePayrollItems(p.getPayrollId(), itemIds);

        return "redirect:/payroll";
    }

    @PostMapping("/payroll/calc")
    public String calc(@RequestParam("id") Long id) {
        payrollService.calculate(id);
        return "redirect:/payroll";
    }

    @PostMapping("/payroll/confirm")
    public String confirm(@RequestParam("id") Long id) {
        payrollService.confirm(id);
        return "redirect:/payroll";
    }

    @GetMapping("/payroll/payslip/{id}")
    public String payslip(@PathVariable("id") Long id,
                          @RequestParam(name = "edit", required = false) String edit,
                          Model model) {

        Payroll payroll = payrollService.findById(id);
        String employeeName = payrollService.getEmployeeName(payroll.getEmployeeId());
        List<PayrollDetail> details = payrollService.findDetails(id);

        boolean editMode = edit != null && !edit.trim().isEmpty();

        model.addAttribute("payroll", payroll);
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("details", details);
        model.addAttribute("editMode", editMode);

        if (editMode && payroll.getStatus() != Payroll.Status.CONFIRMED) {
            List<PayrollItem> items = payrollService.getActiveItems();
            java.util.Set<Long> selectedItemIds = details.stream()
                    .map(PayrollDetail::getItemId)
                    .collect(java.util.stream.Collectors.toSet());

            model.addAttribute("items", items);
            model.addAttribute("selectedItemIds", selectedItemIds);
        }

        return "payment/payroll-payslip";
    }
    
    @PostMapping("/payroll/payslip/{id}/items")
    public String savePayslipItems(@PathVariable("id") Long payrollId,
            @RequestParam(name = "itemIds", required = false) List<Long> itemIds) {

			payrollService.updatePayrollItems(payrollId, itemIds);
			return "redirect:/payroll/payslip/" + payrollId;
	}
}
