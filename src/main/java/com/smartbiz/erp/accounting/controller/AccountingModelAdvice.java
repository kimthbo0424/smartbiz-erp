// src/main/java/com/smartbiz/erp/accounting/controller/AccountingModelAdvice.java
package com.smartbiz.erp.accounting.controller;

import com.smartbiz.erp.accounting.dto.AccountingSummaryView;
import com.smartbiz.erp.accounting.service.AccountingService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = AccountingController.class)
public class AccountingModelAdvice {

    private final AccountingService accountingService;

    public AccountingModelAdvice(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @ModelAttribute("summary")
    public AccountingSummaryView summary() {
        return accountingService.getThisMonthSummary();
    }
}
