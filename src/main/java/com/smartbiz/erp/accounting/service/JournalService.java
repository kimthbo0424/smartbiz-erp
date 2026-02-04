package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.domain.Account;
import com.smartbiz.erp.accounting.domain.JournalEntry;
import com.smartbiz.erp.accounting.domain.JournalItem;
import com.smartbiz.erp.accounting.domain.JournalStatus;
import com.smartbiz.erp.accounting.dto.AccountOptionView;
import com.smartbiz.erp.accounting.dto.JournalCreateForm;
import com.smartbiz.erp.accounting.dto.JournalDetailView;
import com.smartbiz.erp.accounting.dto.JournalEntryRowView;
import com.smartbiz.erp.accounting.repository.AccountRepository;
import com.smartbiz.erp.accounting.repository.JournalEntryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    // ✅ 추가: 마감 정보 조회용
    private final AccountingCloseService closeService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public JournalService(JournalEntryRepository journalEntryRepository,
                          AccountRepository accountRepository,
                          AccountingCloseService closeService) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.closeService = closeService;
    }

    // ------------------------------------------------------------
    // ✅ AccountingController가 기대하는 반환 타입들
    // ------------------------------------------------------------

    /** AccountingController: 최근 전표(무인자 호출) */
    public List<JournalEntryRowView> searchRecentJournals() {
        return searchRecentJournals(null, null, null);
    }

    /** AccountingController: 최근 전표(필터) */
    public List<JournalEntryRowView> searchRecentJournals(String status, String from, String to) {
        JournalStatus st = parseStatus(status);
        LocalDateTime fromDt = parseFrom(from);
        LocalDateTime toDt = parseToExclusive(to);

        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "entryDate"));
        Page<JournalEntry> page = journalEntryRepository.search(st, fromDt, toDt, pageable);

        List<Long> ids = page.getContent().stream()
                .map(JournalEntry::getId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) return List.of();

        // 컬렉션 fetch + paging 경고 회피: 2-step 로딩
        List<JournalEntry> loaded = journalEntryRepository.findWithItemsByIdIn(ids);

        Map<Long, JournalEntry> map = new HashMap<>();
        for (JournalEntry je : loaded) map.put(je.getId(), je);

        List<JournalEntryRowView> rows = new ArrayList<>();
        for (Long id : ids) {
            JournalEntry je = map.get(id);
            if (je == null) continue;

            BigDecimal debitSum = BigDecimal.ZERO;
            BigDecimal creditSum = BigDecimal.ZERO;

            if (je.getItems() != null) {
                for (JournalItem it : je.getItems()) {
                    debitSum = debitSum.add(nz(it.getDebit()));
                    creditSum = creditSum.add(nz(it.getCredit()));
                }
            }

            String statusLabel = mapStatusLabel(je.getStatus());
            String badgeClass = mapBadgeClass(je.getStatus());

            Map<String, Object> props = new HashMap<>();
            props.put("id", je.getId());
            props.put("journalId", je.getId());
            props.put("journalNo", formatJournalNo(je.getId()));
            props.put("entryDate", je.getEntryDate() == null ? null : je.getEntryDate().toLocalDate().format(DATE_FMT));
            props.put("date", je.getEntryDate() == null ? null : je.getEntryDate().toLocalDate().format(DATE_FMT));
            props.put("description", je.getDescription());
            props.put("status", je.getStatus());
            props.put("statusLabel", statusLabel);
            props.put("badgeClass", badgeClass);
            props.put("debitTotal", bdToLong(debitSum));
            props.put("creditTotal", bdToLong(creditSum));
            props.put("referenceType", je.getReferenceType());
            props.put("referenceId", je.getReferenceId());

            rows.add(instantiate(JournalEntryRowView.class, props));
        }

        return rows;
    }

    /** AccountingController: 계정과목 옵션 */
    public List<AccountOptionView> listAccountOptions() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        List<AccountOptionView> opts = new ArrayList<>();
        for (Account a : accounts) {
            Map<String, Object> props = new HashMap<>();
            props.put("id", a.getId());
            props.put("accountId", a.getId());
            props.put("name", a.getName());
            props.put("type", a.getType());
            props.put("label", a.getName());

            opts.add(instantiate(AccountOptionView.class, props));
        }
        return opts;
    }

    /** JournalController: 상세 */
    public JournalDetailView getDetail(Long id) {
        JournalEntry je = journalEntryRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("전표를 찾을 수 없습니다. id=" + id));

        BigDecimal debitSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;

        List<Object> itemViews = new ArrayList<>();
        Class<?> itemViewClass = resolveJournalItemViewClass();

        if (je.getItems() != null) {
            for (JournalItem it : je.getItems()) {
                debitSum = debitSum.add(nz(it.getDebit()));
                creditSum = creditSum.add(nz(it.getCredit()));

                Map<String, Object> ip = new HashMap<>();
                ip.put("id", it.getId());
                ip.put("journalItemId", it.getId());
                ip.put("accountId", it.getAccount() == null ? null : it.getAccount().getId());
                ip.put("accountName", it.getAccount() == null ? null : it.getAccount().getName());
                ip.put("accountType", it.getAccount() == null ? null : it.getAccount().getType());
                ip.put("debit", bdToLong(nz(it.getDebit())));
                ip.put("credit", bdToLong(nz(it.getCredit())));
                ip.put("description", it.getDescription());

                if (itemViewClass != null) {
                    itemViews.add(instantiateRaw(itemViewClass, ip));
                } else {
                    itemViews.add(ip);
                }
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("id", je.getId());
        props.put("journalId", je.getId());
        props.put("journalNo", formatJournalNo(je.getId()));
        props.put("entryDate", je.getEntryDate() == null ? null : je.getEntryDate().toLocalDate().format(DATE_FMT));
        props.put("date", je.getEntryDate() == null ? null : je.getEntryDate().toLocalDate().format(DATE_FMT));
        props.put("description", je.getDescription());
        props.put("status", je.getStatus());
        props.put("statusLabel", mapStatusLabel(je.getStatus()));
        props.put("badgeClass", mapBadgeClass(je.getStatus()));
        props.put("referenceType", je.getReferenceType());
        props.put("referenceId", je.getReferenceId());
        props.put("debitTotal", bdToLong(debitSum));
        props.put("creditTotal", bdToLong(creditSum));
        props.put("items", itemViews);
        props.put("lines", itemViews);

        return instantiate(JournalDetailView.class, props);
    }

    // ------------------------------------------------------------
    // ✅ JournalController: 수기 생성 / 확정
    // ------------------------------------------------------------

    @Transactional
    public Long createManual(JournalCreateForm form) {
        if (form == null) throw new IllegalArgumentException("전표 입력값이 없습니다.");

        JournalEntry je = new JournalEntry();
        je.setEntryDate(parseEntryDateOrNow(getString(form, "getEntryDate", "getDate")));
        je.setDescription(getString(form, "getDescription"));
        je.setStatus(JournalStatus.DRAFT);

        String refType = getString(form, "getReferenceType");
        Long refId = getLong(form, "getReferenceId");
        if (refType != null && !refType.isBlank()) je.setReferenceType(refType.trim());
        if (refId != null) je.setReferenceId(refId);

        List<?> lines = getList(form, "getLines", "getItems");
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("전표 라인이 1개 이상 필요합니다.");
        }

        BigDecimal debitSum = BigDecimal.ZERO;
        BigDecimal creditSum = BigDecimal.ZERO;

        for (Object line : lines) {
            Integer accountId = getInteger(line, "getAccountId", "getAccount_id");
            if (accountId == null) {
                throw new IllegalArgumentException("계정과목(accountId)이 누락된 라인이 있습니다.");
            }

            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정과목입니다. id=" + accountId));

            BigDecimal debit = money(getBigDecimal(line, "getDebit", "getDebitAmount"));
            BigDecimal credit = money(getBigDecimal(line, "getCredit", "getCreditAmount"));

            boolean debitPos = debit.compareTo(BigDecimal.ZERO) > 0;
            boolean creditPos = credit.compareTo(BigDecimal.ZERO) > 0;
            if (debitPos == creditPos) {
                throw new IllegalArgumentException("각 라인은 차변 또는 대변 중 하나만 입력해야 합니다.");
            }

            JournalItem item = new JournalItem();
            item.setAccount(account);
            item.setDebit(debit);
            item.setCredit(credit);
            item.setDescription(getString(line, "getDescription", "getLineDescription"));

            je.addItem(item);

            debitSum = debitSum.add(debit);
            creditSum = creditSum.add(credit);
        }

        if (debitSum.compareTo(creditSum) != 0) {
            throw new IllegalArgumentException("차변 합계와 대변 합계가 일치해야 합니다. (차변=" + debitSum + ", 대변=" + creditSum + ")");
        }

        JournalEntry saved = journalEntryRepository.save(je);
        return saved.getId();
    }

    @Transactional
    public void post(Long id) {
        JournalEntry je = journalEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("전표를 찾을 수 없습니다. id=" + id));

        if (je.getStatus() == JournalStatus.POSTED || je.getStatus() == JournalStatus.CLOSED) return;

        // ✅ 마감 이후 전표 확정 제한 (가장 최근 CLOSED의 closedTo 기준)
        LocalDate latestClosedTo = closeService.getLatestClosedTo().orElse(null);
        if (latestClosedTo != null) {
            if (je.getEntryDate() == null) {
                throw new IllegalStateException("전표 일자가 없어 확정할 수 없습니다.");
            }
            LocalDate entryDate = je.getEntryDate().toLocalDate();

            // 마감 기준일 이하(<=)는 확정 불가
            if (!entryDate.isAfter(latestClosedTo)) {
                throw new IllegalStateException(
                        "마감된 기간(" + latestClosedTo + ")의 전표는 확정할 수 없습니다. 역마감 후 진행하세요."
                );
            }
        }

        je.setStatus(JournalStatus.POSTED);
        journalEntryRepository.save(je);
    }

    // ------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------

    private static String formatJournalNo(Long id) {
        if (id == null) return "";
        return String.format("JV-%06d", id);
    }

    private static String mapStatusLabel(JournalStatus status) {
        if (status == null) return "미지정";
        return switch (status) {
            case DRAFT -> "미승인";
            case POSTED -> "확정";
            case CLOSED -> "마감";
        };
    }

    private static String mapBadgeClass(JournalStatus status) {
        if (status == null) return "bg-secondary";
        return switch (status) {
            case DRAFT -> "bg-warning text-dark";
            case POSTED -> "bg-success";
            case CLOSED -> "bg-secondary";
        };
    }

    private static JournalStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return JournalStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static LocalDateTime parseFrom(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return null;
        LocalDate d = LocalDate.parse(yyyyMmDd.trim(), DATE_FMT);
        return d.atStartOfDay();
    }

    private static LocalDateTime parseToExclusive(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return null;
        LocalDate d = LocalDate.parse(yyyyMmDd.trim(), DATE_FMT);
        return d.plusDays(1).atStartOfDay();
    }

    private static LocalDateTime parseEntryDateOrNow(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return LocalDateTime.now();
        LocalDate d = LocalDate.parse(yyyyMmDd.trim(), DATE_FMT);
        return d.atStartOfDay();
    }

    private static BigDecimal money(BigDecimal bd) {
        if (bd == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return bd.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal bd) {
        return bd == null ? BigDecimal.ZERO : bd;
    }

    private static long bdToLong(BigDecimal bd) {
        if (bd == null) return 0L;
        return bd.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    // ------------------------------------------------------------
    // ✅ DTO 생성(프로젝트 DTO의 생성자/필드가 달라도 최대한 맞춰 넣기)
    // ------------------------------------------------------------

    private static <T> T instantiate(Class<T> type, Map<String, Object> props) {
        Object obj = instantiateRaw(type, props);
        return type.cast(obj);
    }

    private static Object instantiateRaw(Class<?> type, Map<String, Object> props) {
        // 1) no-args + setter
        try {
            Constructor<?> c0 = type.getDeclaredConstructor();
            c0.setAccessible(true);
            Object inst = c0.newInstance();

            for (Map.Entry<String, Object> e : props.entrySet()) {
                String prop = e.getKey();
                Object val = e.getValue();
                String setter = "set" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);

                for (Method m : type.getMethods()) {
                    if (!m.getName().equals(setter)) continue;
                    if (m.getParameterCount() != 1) continue;

                    Object converted = convertValue(val, m.getParameterTypes()[0]);
                    if (converted == null && val != null && m.getParameterTypes()[0].isPrimitive()) continue;

                    m.invoke(inst, converted);
                    break;
                }
            }
            return inst;
        } catch (NoSuchMethodException ignored) {
            // no default ctor -> try constructor mapping
        } catch (Exception e) {
            throw new IllegalStateException("DTO 생성 실패(no-args+setter): " + type.getSimpleName(), e);
        }

        // 2) constructor param name matching
        Constructor<?> best = null;
        Object[] bestArgs = null;

        for (Constructor<?> ctor : type.getDeclaredConstructors()) {
            ctor.setAccessible(true);
            var params = ctor.getParameters();

            Object[] args = new Object[params.length];
            boolean ok = true;

            for (int i = 0; i < params.length; i++) {
                String pn = params[i].getName();
                Class<?> pt = params[i].getType();

                Object raw = props.get(pn);
                Object cvt = convertValue(raw, pt);

                if (cvt == null && raw != null && pt.isPrimitive()) {
                    ok = false;
                    break;
                }
                if (raw == null && pt.isPrimitive()) {
                    ok = false;
                    break;
                }
                args[i] = cvt;
            }

            if (!ok) continue;

            if (best == null || params.length > best.getParameterCount()) {
                best = ctor;
                bestArgs = args;
            }
        }

        if (best == null) {
            throw new IllegalStateException("DTO 생성 실패(생성자 매칭 불가): " + type.getName()
                    + " / props=" + props.keySet());
        }

        try {
            return best.newInstance(bestArgs);
        } catch (Exception e) {
            throw new IllegalStateException("DTO 생성 실패(생성자 호출): " + type.getName(), e);
        }
    }

    private static Object convertValue(Object val, Class<?> targetType) {
        if (val == null) return null;

        if (targetType.isInstance(val)) return val;

        if (targetType == Long.class || targetType == long.class) {
            if (val instanceof Number n) return n.longValue();
            try { return Long.parseLong(String.valueOf(val)); } catch (Exception ignored) { return null; }
        }
        if (targetType == Integer.class || targetType == int.class) {
            if (val instanceof Number n) return n.intValue();
            try { return Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) { return null; }
        }
        if (targetType == String.class) {
            return String.valueOf(val);
        }
        if (targetType.isEnum()) {
            if (val instanceof String s) {
                try {
                    @SuppressWarnings({"rawtypes","unchecked"})
                    Object ev = Enum.valueOf((Class<? extends Enum>) targetType, s.trim().toUpperCase());
                    return ev;
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        if (List.class.isAssignableFrom(targetType) && val instanceof List<?> list) {
            return list;
        }

        return null;
    }

    /** 전표 라인 DTO 클래스가 있으면 사용 (없으면 null) */
    private static Class<?> resolveJournalItemViewClass() {
        String[] candidates = new String[] {
                "com.smartbiz.erp.accounting.dto.JournalItemLineView",
                "com.smartbiz.erp.accounting.dto.JournalItemRowView",
                "com.smartbiz.erp.accounting.dto.JournalLineView",
                "com.smartbiz.erp.accounting.dto.JournalItemView"
        };
        for (String cn : candidates) {
            try {
                return Class.forName(cn);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static String getString(Object target, String... getterNames) {
        Object v = invokeFirst(target, getterNames);
        return (v == null) ? null : String.valueOf(v);
    }

    private static Long getLong(Object target, String... getterNames) {
        Object v = invokeFirst(target, getterNames);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static Integer getInteger(Object target, String... getterNames) {
        Object v = invokeFirst(target, getterNames);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static BigDecimal getBigDecimal(Object target, String... getterNames) {
        Object v = invokeFirst(target, getterNames);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static List<?> getList(Object target, String... getterNames) {
        Object v = invokeFirst(target, getterNames);
        if (v instanceof List<?> list) return list;
        return null;
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
}
