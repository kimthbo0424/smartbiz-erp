// src/main/java/com/smartbiz/erp/accounting/controller/ExpenseItemController.java
package com.smartbiz.erp.accounting.controller;

import com.smartbiz.erp.accounting.dto.ExpenseItemCreateForm;
import com.smartbiz.erp.accounting.service.ExpenseItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounting/expense-items")
public class ExpenseItemController {

    private final ExpenseItemService expenseItemService;

    public ExpenseItemController(ExpenseItemService expenseItemService) {
        this.expenseItemService = expenseItemService;
    }

    @PostMapping
    public String create(@ModelAttribute ExpenseItemCreateForm form, RedirectAttributes ra) {
        try {
            Long id = expenseItemService.create(form);
            ra.addFlashAttribute("expenseMessage", "비용 항목이 등록되었습니다. (" + id + ")");
            return "redirect:/accounting?tab=expense";
        } catch (Exception e) {
            ra.addFlashAttribute("expenseError", e.getMessage());
            return "redirect:/accounting?tab=expense";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            expenseItemService.toggleActive(id);
            ra.addFlashAttribute("expenseMessage", "비용 항목 상태가 변경되었습니다. (" + id + ")");
            return "redirect:/accounting?tab=expense";
        } catch (Exception e) {
            ra.addFlashAttribute("expenseError", e.getMessage());
            return "redirect:/accounting?tab=expense";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            expenseItemService.deactivate(id);
            ra.addFlashAttribute("expenseMessage", "비용 항목이 비활성 처리되었습니다. (" + id + ")");
            return "redirect:/accounting?tab=expense";
        } catch (Exception e) {
            ra.addFlashAttribute("expenseError", e.getMessage());
            return "redirect:/accounting?tab=expense";
        }
    }
}
