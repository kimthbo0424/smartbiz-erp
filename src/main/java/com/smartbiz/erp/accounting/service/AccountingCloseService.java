package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.domain.AccountingClose;
import com.smartbiz.erp.accounting.domain.AccountingCloseStatus;
import com.smartbiz.erp.accounting.domain.AccountingCloseType;
import com.smartbiz.erp.accounting.dto.CloseCreateForm;
import com.smartbiz.erp.accounting.dto.CloseRowView;
import com.smartbiz.erp.accounting.repository.AccountingCloseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountingCloseService {

    private final AccountingCloseRepository closeRepository;

    public AccountingCloseService(AccountingCloseRepository closeRepository) {
        this.closeRepository = closeRepository;
    }

    public Optional<LocalDate> getLatestClosedTo() {
        return closeRepository.findLatestClosedOne().map(AccountingClose::getClosedTo);
    }

    public List<CloseRowView> listHistory() {
        List<AccountingClose> list = closeRepository.findTop50ByOrderByClosedAtDesc();
        List<CloseRowView> out = new ArrayList<>();
        for (AccountingClose c : list) {
            out.add(CloseRowView.from(c));
        }
        return out;
    }

    /**
     * ✅ 1차 정책:
     * - 최신 마감 기준일(closedTo) 이하(=이전 포함) 전표는 확정(POST) 불가
     * - 즉 entryDate 날짜가 closedTo "초과"일 때만 POST 가능
     */
    public void validatePostable(LocalDateTime entryDate) {
        if (entryDate == null) return;

        Optional<LocalDate> latest = getLatestClosedTo();
        if (latest.isEmpty()) return;

        LocalDate closedTo = latest.get();
        LocalDate entry = entryDate.toLocalDate();

        if (!entry.isAfter(closedTo)) {
            throw new IllegalStateException(
                    "마감된 기간(" + closedTo + ") 이전 전표는 확정(POST)할 수 없습니다. 전표일자=" + entry
            );
        }
    }

    @Transactional
    public void closePeriod(CloseCreateForm form, String userName) {
        AccountingCloseType type = parseType(form.getType());
        String periodKey = normalizePeriodKey(type, form.getPeriod());

        if (closeRepository.existsByPeriodKeyAndStatus(periodKey, AccountingCloseStatus.CLOSED)) {
            throw new IllegalStateException("이미 마감된 기간입니다: " + periodKey);
        }

        LocalDate closedTo = computeClosedTo(type, periodKey);
        AccountingClose close = new AccountingClose(type, periodKey, closedTo, userName);

        closeRepository.save(close);
    }

    @Transactional
    public void reverse(Long id, String userName) {
        AccountingClose c = closeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("마감 이력을 찾을 수 없습니다. id=" + id));

        if (c.getStatus() != AccountingCloseStatus.CLOSED) {
            throw new IllegalStateException("이미 역마감 처리된 건입니다.");
        }

        c.reverse(userName);
        closeRepository.save(c);
    }

    private AccountingCloseType parseType(String v) {
        if (v == null || v.isBlank()) return AccountingCloseType.MONTH;
        return AccountingCloseType.valueOf(v.trim().toUpperCase());
    }

    private String normalizePeriodKey(AccountingCloseType type, String raw) {
        if (raw == null || raw.isBlank()) {
            LocalDate now = LocalDate.now();
            if (type == AccountingCloseType.MONTH) {
                YearMonth ym = YearMonth.from(now);
                return ym.toString();
            }
            if (type == AccountingCloseType.YEAR) {
                return String.valueOf(now.getYear());
            }
            int q = (now.getMonthValue() - 1) / 3 + 1;
            return now.getYear() + "-Q" + q;
        }

        String t = raw.trim();

        if (type == AccountingCloseType.MONTH) {
            YearMonth.parse(t);
            return t;
        }

        if (type == AccountingCloseType.YEAR) {
            Year.parse(t);
            return t;
        }

        if (!t.matches("^\\d{4}-Q[1-4]$")) {
            throw new IllegalArgumentException("분기 형식이 올바르지 않습니다. 예: 2026-Q1");
        }
        return t;
    }

    private LocalDate computeClosedTo(AccountingCloseType type, String periodKey) {
        if (type == AccountingCloseType.MONTH) {
            YearMonth ym = YearMonth.parse(periodKey);
            return ym.atEndOfMonth();
        }

        if (type == AccountingCloseType.YEAR) {
            int y = Integer.parseInt(periodKey);
            return LocalDate.of(y, 12, 31);
        }

        int year = Integer.parseInt(periodKey.substring(0, 4));
        int q = Integer.parseInt(periodKey.substring(6, 7));
        int endMonth = q * 3;
        YearMonth ym = YearMonth.of(year, endMonth);
        return ym.atEndOfMonth();
    }
}
