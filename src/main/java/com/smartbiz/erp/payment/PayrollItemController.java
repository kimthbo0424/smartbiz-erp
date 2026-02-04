package com.smartbiz.erp.payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PayrollItemController {

    private final PayrollItemService payrollItemService;

    @GetMapping("/payroll-items")
    public String list(Model model) {
        model.addAttribute("items", payrollItemService.findAll());
        return "payment/payroll-items";
    }

    @GetMapping("/payroll-items/form")
    public String form(@RequestParam(value = "id", required = false) Long id, Model model) {

        PayrollItem item;
        if (id == null) {
            item = new PayrollItem();
            item.setUseYn("Y");
        } else {
            item = payrollItemService.findById(id);
        }

        model.addAttribute("item", item);
        model.addAttribute("itemTypes", PayrollItem.ItemType.values());
        model.addAttribute("calcTypes", PayrollItem.CalcType.values());

        return "payment/payroll-item-form";
    }

    @PostMapping("/payroll-items/save")
    public String save(@ModelAttribute("item") PayrollItem item) {
        payrollItemService.save(item);
        return "redirect:/payroll-items";
    }

    @PostMapping("/payroll-items/delete")
    public String delete(@RequestParam("id") Long id) {
        payrollItemService.delete(id);
        return "redirect:/payroll-items";
    }
}
