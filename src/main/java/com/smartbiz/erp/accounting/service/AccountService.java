// src/main/java/com/smartbiz/erp/accounting/service/AccountService.java
package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.domain.Account;
import com.smartbiz.erp.accounting.dto.*;
import com.smartbiz.erp.accounting.repository.AccountRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountRowView> listAccounts() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        List<AccountRowView> rows = new ArrayList<>();
        for (Account a : accounts) {
            int id = toInt(invokeFirst(a, "getId"), 0);
            String name = toStr(invokeFirst(a, "getName"));
            Object typeObj = invokeFirst(a, "getType");
            String type = (typeObj == null) ? "" : String.valueOf(typeObj);
            String typeLabel = mapTypeLabel(type);

            Account parent = (Account) invokeFirst(a, "getParent");
            Integer parentId = null;
            String parentName = null;
            if (parent != null) {
                parentId = toInt(invokeFirst(parent, "getId"), 0);
                parentName = toStr(invokeFirst(parent, "getName"));
            }

            Boolean active = toBool(invokeFirst(a, "getActive", "isActive"));
            boolean activeVal = (active == null) ? true : active;

            rows.add(new AccountRowView(
                    id,
                    name,
                    type,
                    typeLabel,
                    parentId,
                    parentName,
                    activeVal
            ));
        }

        return rows;
    }

    @Transactional
    public int create(AccountCreateForm form) {
        if (form == null) throw new IllegalArgumentException("입력값이 없습니다.");
        String name = (form.getName() == null) ? "" : form.getName().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("계정명은 필수입니다.");

        Integer id = form.getId();
        if (id != null && accountRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 존재하는 계정 코드입니다. (" + id + ")");
        }

        Account a = new Account();

        if (id != null) {
            invokeSetter(a, "setId", id);
        }
        invokeSetter(a, "setName", name);

        String type = (form.getType() == null) ? "" : form.getType().trim();
        if (!type.isEmpty()) {
            setTypeFlex(a, type);
        }

        if (form.getParentId() != null) {
            Account parent = accountRepository.findById(form.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위계정을 찾을 수 없습니다. id=" + form.getParentId()));
            invokeSetter(a, "setParent", parent);
        }

        if (form.getActive() != null) {
            invokeSetter(a, "setActive", form.getActive());
        }

        Account saved = accountRepository.save(a);
        return toInt(invokeFirst(saved, "getId"), id != null ? id : 0);
    }

    @Transactional(readOnly = true)
    public List<AccountTypeOptionView> listTypeOptions() {
        Class<?> enumType = resolveAccountTypeEnum();
        if (enumType != null && enumType.isEnum()) {
            Object[] constants = enumType.getEnumConstants();
            List<AccountTypeOptionView> opts = new ArrayList<>();
            for (Object c : constants) {
                String v = String.valueOf(c);
                opts.add(new AccountTypeOptionView(v, mapTypeLabel(v)));
            }
            return opts;
        }

        return List.of(
                new AccountTypeOptionView("ASSET", mapTypeLabel("ASSET")),
                new AccountTypeOptionView("LIABILITY", mapTypeLabel("LIABILITY")),
                new AccountTypeOptionView("EQUITY", mapTypeLabel("EQUITY")),
                new AccountTypeOptionView("REVENUE", mapTypeLabel("REVENUE")),
                new AccountTypeOptionView("EXPENSE", mapTypeLabel("EXPENSE"))
        );
    }

    // -------------------------
    // helpers
    // -------------------------

    private static String mapTypeLabel(String type) {
        String t = (type == null) ? "" : type.trim().toUpperCase();
        return switch (t) {
            case "ASSET", "자산" -> "자산";
            case "LIABILITY", "부채" -> "부채";
            case "EQUITY", "자본" -> "자본";
            case "REVENUE", "INCOME", "수익" -> "수익";
            case "EXPENSE", "COST", "비용" -> "비용";
            default -> (type == null || type.isBlank()) ? "-" : type;
        };
    }

    private static Class<?> resolveAccountTypeEnum() {
        String[] candidates = new String[] {
                "com.smartbiz.erp.accounting.domain.AccountType",
                "com.smartbiz.erp.accounting.domain.CoaType"
        };
        for (String cn : candidates) {
            try {
                return Class.forName(cn);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static void setTypeFlex(Account target, String typeName) {
        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals("setType")) continue;
            if (m.getParameterCount() != 1) continue;

            Class<?> pt = m.getParameterTypes()[0];
            try {
                if (pt == String.class) {
                    m.invoke(target, typeName);
                    return;
                }
                if (pt.isEnum()) {
                    @SuppressWarnings({"rawtypes","unchecked"})
                    Object ev = Enum.valueOf((Class<? extends Enum>) pt, typeName.trim().toUpperCase());
                    m.invoke(target, ev);
                    return;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("유효하지 않은 계정 유형(type)입니다: " + typeName, e);
            }
        }
    }

    private static void invokeSetter(Object target, String setterName, Object value) {
        if (target == null) return;
        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals(setterName)) continue;
            if (m.getParameterCount() != 1) continue;
            try {
                Class<?> pt = m.getParameterTypes()[0];
                Object converted = convert(value, pt);
                if (converted == null && value != null && pt.isPrimitive()) continue;
                m.invoke(target, converted);
                return;
            } catch (Exception ignored) { }
        }
    }

    private static Object invokeFirst(Object target, String... getterNames) {
        if (target == null) return null;
        for (String name : getterNames) {
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Object convert(Object val, Class<?> targetType) {
        if (val == null) return null;
        if (targetType.isInstance(val)) return val;

        if (targetType == Integer.class || targetType == int.class) {
            if (val instanceof Number n) return n.intValue();
            try { return Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) { return null; }
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (val instanceof Boolean b) return b;
            String s = String.valueOf(val).trim().toLowerCase();
            if (s.equals("true") || s.equals("1") || s.equals("y")) return true;
            if (s.equals("false") || s.equals("0") || s.equals("n")) return false;
            return null;
        }
        if (targetType == String.class) return String.valueOf(val);

        return null;
    }

    private static int toInt(Object v, int def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    private static String toStr(Object v) {
        return (v == null) ? null : String.valueOf(v);
    }

    private static Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        String s = String.valueOf(v).trim().toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("y")) return true;
        if (s.equals("false") || s.equals("0") || s.equals("n")) return false;
        return null;
    }
}
