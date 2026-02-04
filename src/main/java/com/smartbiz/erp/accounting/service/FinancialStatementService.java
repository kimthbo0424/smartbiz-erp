package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.dto.BalanceSheetView;
import com.smartbiz.erp.accounting.dto.IncomeStatementView;
import com.smartbiz.erp.accounting.repository.JournalItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class FinancialStatementService {

    private final JournalItemRepository journalItemRepository;

    public FinancialStatementService(JournalItemRepository journalItemRepository) {
        this.journalItemRepository = journalItemRepository;
    }

    // -----------------------
    // Balance Sheet
    // -----------------------

    // LocalDateTime API
    public BalanceSheetView getBalanceSheet(LocalDateTime asOf) {
        LocalDateTime 기준 = (asOf == null) ? LocalDateTime.now() : asOf;

        long assets = journalItemRepository.sumAssets(기준);
        long liabilities = journalItemRepository.sumLiabilities(기준);

        // BalanceSheetView에 (assets, liabilities) 오버로드가 있으므로 그대로 사용 가능
        return new BalanceSheetView(assets, liabilities);
    }

    // LocalDate API (Controller 호환)
    public BalanceSheetView getBalanceSheet(LocalDate asOfDate) {
        LocalDate d = (asOfDate == null) ? LocalDate.now() : asOfDate;

        // ✅ 같은 날짜의 모든 시각을 포함시키기 위해 하루의 "끝"으로 맞춤
        // (DB가 microsecond 정밀도를 갖는 경우 23:59:59로 자르면 누락 위험)
        LocalDateTime asOf = d.plusDays(1).atStartOfDay().minusNanos(1);

        return getBalanceSheet(asOf);
    }

    // -----------------------
    // Income Statement
    // -----------------------

    // LocalDateTime API
    public IncomeStatementView getIncomeStatement(LocalDateTime from, LocalDateTime to) {
        LocalDateTime f = (from == null) ? LocalDate.now().withDayOfMonth(1).atStartOfDay() : from;
        LocalDateTime t = (to == null) ? LocalDateTime.now() : to;

        if (t.isBefore(f)) {
            LocalDateTime tmp = f;
            f = t;
            t = tmp;
        }

        long revenue = journalItemRepository.sumRevenue(f, t);
        long expense = journalItemRepository.sumExpense(f, t);

        return new IncomeStatementView(revenue, expense);
    }

    // LocalDate API (Controller 호환)
    public IncomeStatementView getIncomeStatement(LocalDate fromDate, LocalDate toDate) {
        LocalDate f = (fromDate == null) ? LocalDate.now().withDayOfMonth(1) : fromDate;
        LocalDate t = (toDate == null) ? LocalDate.now() : toDate;

        if (t.isBefore(f)) {
            LocalDate tmp = f;
            f = t;
            t = tmp;
        }

        LocalDateTime from = f.atStartOfDay();
        LocalDateTime to = t.plusDays(1).atStartOfDay().minusNanos(1);

        return getIncomeStatement(from, to);
    }
}
