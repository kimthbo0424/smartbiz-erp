package com.smartbiz.erp.accounting.controller;

import com.smartbiz.erp.accounting.domain.ExpenseCycle;
import com.smartbiz.erp.accounting.dto.*;
import com.smartbiz.erp.accounting.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/accounting")
public class AccountingController {

    private final AccountingService accountingService;
    private final AccountService accountService;
    private final JournalService journalService;
    private final ExpenseItemService expenseItemService;
    private final FinancialStatementService financialStatementService;
    private final VatService vatService;
    private final AccountingCloseService closeService;

    public AccountingController(AccountingService accountingService,
                                AccountService accountService,
                                JournalService journalService,
                                ExpenseItemService expenseItemService,
                                FinancialStatementService financialStatementService,
                                VatService vatService,
                                AccountingCloseService closeService) {
        this.accountingService = accountingService;
        this.accountService = accountService;
        this.journalService = journalService;
        this.expenseItemService = expenseItemService;
        this.financialStatementService = financialStatementService;
        this.vatService = vatService;
        this.closeService = closeService;
    }

    @ModelAttribute("journalForm")
    public JournalCreateForm journalForm() {
        JournalCreateForm f = new JournalCreateForm();
        f.getLines().add(new JournalLineForm());
        f.getLines().add(new JournalLineForm());
        return f;
    }

    @ModelAttribute("expenseForm")
    public ExpenseItemCreateForm expenseForm() {
        ExpenseItemCreateForm f = new ExpenseItemCreateForm();
        f.setCycle(ExpenseCycle.MONTHLY);
        f.setActive(true);
        return f;
    }

    @ModelAttribute("accountForm")
    public AccountCreateForm accountForm() {
        AccountCreateForm f = new AccountCreateForm();
        f.setActive(true);
        return f;
    }

    @ModelAttribute("closeForm")
    public CloseCreateForm closeForm() {
        CloseCreateForm f = new CloseCreateForm();
        f.setType("MONTH");
        f.setPeriod(YearMonth.now().toString());
        return f;
    }

    @GetMapping
    public String page(@ModelAttribute("q") AccountingSearchQuery q,
                       @ModelAttribute("pq") ProfitReportQuery pq,
                       @ModelAttribute("fsq") FinancialStatementQuery fsq,
                       @ModelAttribute("vq") VatReportQuery vq,
                       @RequestParam(name = "tab", required = false) String tab,
                       Model model) {

        // ✅ 재무제표 기본 기간
        if (fsq.getFsAsOf() == null) fsq.setFsAsOf(LocalDate.now());
        if (fsq.getFsFrom() == null) fsq.setFsFrom(LocalDate.now().withDayOfMonth(1));
        if (fsq.getFsTo() == null) fsq.setFsTo(LocalDate.now());

        // 상단 요약 + 매출/매입 내역(orders 기반)
        AccountingSummaryView summary = accountingService.getThisMonthSummary();
        List<AccountingTransactionRowView> rows = accountingService.searchTransactions(q);

        // 손익 리포트
        ProfitReportSummaryView profitSummary = accountingService.getProfitSummary(pq);
        List<ProfitReportRowView> profitRows = accountingService.getProfitRows(pq);

        // 전표 탭
        List<JournalEntryRowView> journalRows = journalService.searchRecentJournals();
        List<AccountOptionView> accounts = journalService.listAccountOptions();

        // 계정과목 탭
        List<AccountRowView> accountRows = accountService.listAccounts();
        List<AccountTypeOptionView> accountTypeOptions = accountService.listTypeOptions();

        // 비용항목 탭
        List<ExpenseItemRowView> expenseItems = expenseItemService.listItems();

        // ✅ 재무제표 탭
        model.addAttribute("balanceSheet", financialStatementService.getBalanceSheet(fsq.getFsAsOf()));
        model.addAttribute("incomeStatement", financialStatementService.getIncomeStatement(fsq.getFsFrom(), fsq.getFsTo()));

        // VAT 탭
        VatSummaryView vatSummary = vatService.getSummary(vq);
        List<VatRowView> vatRows = vatService.getRows(vq);

        // 마감 탭
        List<CloseRowView> closeHistory = closeService.listHistory();

        // ✅ 최신 마감 기준일(문자열: yyyy-MM-dd). UI에서 승인 버튼 숨김 조건에 사용
        String latestClosedTo = closeService.getLatestClosedTo().map(LocalDate::toString).orElse(null);

        model.addAttribute("pageTitle", "SmartBiz ERP - 회계관리");
        model.addAttribute("summary", summary);
        model.addAttribute("rows", rows);

        model.addAttribute("profitSummary", profitSummary);
        model.addAttribute("profitRows", profitRows);

        model.addAttribute("journalRows", journalRows);
        model.addAttribute("accounts", accounts);

        model.addAttribute("accountRows", accountRows);
        model.addAttribute("accountTypeOptions", accountTypeOptions);

        model.addAttribute("expenseItems", expenseItems);

        model.addAttribute("vatSummary", vatSummary);
        model.addAttribute("vatRows", vatRows);

        model.addAttribute("closeHistory", closeHistory);

        model.addAttribute("latestClosedTo", latestClosedTo);

        model.addAttribute("activeTab", (tab == null || tab.isBlank()) ? "trans" : tab);

        return "accounting/accounting";
    }

    @PostMapping("/close")
    public String close(@ModelAttribute("closeForm") CloseCreateForm form, RedirectAttributes ra) {
        try {
            closeService.closePeriod(form, "admin");
            ra.addFlashAttribute("closeMessage", "마감이 완료되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("closeError", e.getMessage());
        }
        return "redirect:/accounting?tab=close";
    }

    @PostMapping("/close/{id}/reverse")
    public String reverse(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            closeService.reverse(id, "admin");
            ra.addFlashAttribute("closeMessage", "역마감이 완료되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("closeError", e.getMessage());
        }
        return "redirect:/accounting?tab=close";
    }
}
