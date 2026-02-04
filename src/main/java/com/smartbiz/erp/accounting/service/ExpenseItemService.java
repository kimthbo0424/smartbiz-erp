package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.domain.ExpenseCycle;
import com.smartbiz.erp.accounting.domain.ExpenseItem;
import com.smartbiz.erp.accounting.dto.ExpenseItemCreateForm;
import com.smartbiz.erp.accounting.dto.ExpenseItemRowView;
import com.smartbiz.erp.accounting.repository.ExpenseItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseItemService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseItemService.class);

    private final ExpenseItemRepository expenseItemRepository;

    public ExpenseItemService(ExpenseItemRepository expenseItemRepository) {
        this.expenseItemRepository = expenseItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseItemRowView> listItems() {
        List<ExpenseItem> items;
        try {
            items = expenseItemRepository.findAll();
        } catch (DataAccessException e) {
            // expense_item 테이블이 아직 없을 때 화면이 죽지 않게 방어
            if (isMissingExpenseTable(e)) {
                log.warn("expense_item table is missing. Returning empty expenseItems list.");
                return List.of();
            }
            throw e;
        }

        List<ExpenseItemRowView> rows = new ArrayList<>();
        for (ExpenseItem it : items) {
            ExpenseCycle cycle = it.getCycle();
            String cycleLabel = mapCycleLabel(cycle);

            rows.add(new ExpenseItemRowView(
                    it.getId(),
                    it.getName(),
                    cycle == null ? null : cycle.name(),
                    cycleLabel,
                    Boolean.TRUE.equals(it.getActive())
            ));
        }
        return rows;
    }

    @Transactional
    public Long create(ExpenseItemCreateForm form) {
        if (form == null) throw new IllegalArgumentException("비용 항목 입력값이 없습니다.");

        String name = safeTrim(form.getName());
        if (name == null) throw new IllegalArgumentException("비용 항목명(name)은 필수입니다.");

        ExpenseCycle cycle = (form.getCycle() == null) ? ExpenseCycle.MONTHLY : form.getCycle();
        Boolean active = (form.getActive() == null) ? Boolean.TRUE : form.getActive();

        // ExpenseItemCreateForm에 description getter가 없을 수 있음
        String desc = safeTrim(getOptionalString(form, "getDescription", "getMemo", "getNote"));

        ExpenseItem it = new ExpenseItem();
        it.setName(name);
        it.setCycle(cycle);
        it.setActive(active);

        // ✅ ExpenseItem 엔티티에 description 필드가 있을 때만 주입 (없으면 스킵)
        if (desc != null) {
            invokeOptionalSetter(it, desc, "setDescription", "setMemo", "setNote");
        }

        return expenseItemRepository.save(it).getId();
    }

    @Transactional
    public void toggleActive(Long id) {
        ExpenseItem it = expenseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("비용 항목을 찾을 수 없습니다. id=" + id));

        boolean cur = Boolean.TRUE.equals(it.getActive());
        it.setActive(!cur);
        expenseItemRepository.save(it);
    }

    @Transactional
    public void deactivate(Long id) {
        ExpenseItem it = expenseItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("비용 항목을 찾을 수 없습니다. id=" + id));

        it.setActive(false);
        expenseItemRepository.save(it);
    }

    // ---------------- helpers ----------------

    private static String mapCycleLabel(ExpenseCycle cycle) {
        if (cycle == null) return "";
        return switch (cycle) {
            case MONTHLY -> "월별";
            case ONCE -> "일시";
        };
    }

    private static boolean isMissingExpenseTable(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null) {
                String m = msg.toLowerCase();
                if (m.contains("doesn't exist") && m.contains("expense_item")) return true;
                if (m.contains("table") && m.contains("expense_item") && m.contains("does not exist")) return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String getOptionalString(Object target, String... getterNames) {
        if (target == null) return null;
        for (String name : getterNames) {
            try {
                Method m = target.getClass().getMethod(name);
                Object v = m.invoke(target);
                if (v != null) return String.valueOf(v);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void invokeOptionalSetter(Object target, String value, String... setterNames) {
        if (target == null) return;
        for (String name : setterNames) {
            try {
                Method m = target.getClass().getMethod(name, String.class);
                m.invoke(target, value);
                return; // 첫 성공만 적용
            } catch (Exception ignored) {
            }
        }
    }
}
