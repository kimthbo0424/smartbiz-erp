// src/main/java/com/smartbiz/erp/accounting/controller/AccountController.java
package com.smartbiz.erp.accounting.controller;

import com.smartbiz.erp.accounting.dto.AccountCreateForm;
import com.smartbiz.erp.accounting.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounting/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public String create(@ModelAttribute AccountCreateForm form,
                         RedirectAttributes ra) {
        try {
            int id = accountService.create(form);
            ra.addFlashAttribute("accountMessage", "계정과목이 등록되었습니다. (코드: " + id + ")");
        } catch (Exception e) {
            ra.addFlashAttribute("accountError", e.getMessage());
        }
        return "redirect:/accounting?tab=coa";
    }
}
