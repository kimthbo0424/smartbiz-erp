package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.domain.Account;
import com.smartbiz.erp.accounting.domain.JournalEntry;
import com.smartbiz.erp.accounting.domain.JournalItem;
import com.smartbiz.erp.accounting.domain.JournalStatus;
import com.smartbiz.erp.accounting.dto.*;
import com.smartbiz.erp.accounting.repository.JournalEntryRepository;
import com.smartbiz.erp.accounting.repository.JournalItemRepository;
import com.smartbiz.erp.orders.domain.Order;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AccountingService {

    private final OrderRepository orderRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalItemRepository journalItemRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AccountingService(OrderRepository orderRepository,
                             JournalEntryRepository journalEntryRepository,
                             JournalItemRepository journalItemRepository) {
        this.orderRepository = orderRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalItemRepository = journalItemRepository;
    }

    public AccountingSummaryView getThisMonthSummary() {
        LocalDate today = LocalDate.now();
        LocalDate monthStartDate = today.withDayOfMonth(1);
        LocalDate monthEndDate = monthStartDate.plusMonths(1);

        LocalDateTime from = monthStartDate.atStartOfDay();
        LocalDateTime to = monthEndDate.atStartOfDay();

        // ---------------- 매출(주문) 요약 ----------------
        BigDecimal salesTotalBd = orderRepository.sumTotalAmountBetween(from, to, OrderStatus.SETTLED);
        BigDecimal salesVatBd = orderRepository.sumTaxAmountBetween(from, to, OrderStatus.SETTLED);

        long monthSalesTotal = bdToLong(salesTotalBd);
        long expectedVat = bdToLong(salesVatBd);

        long unprocessed = orderRepository.countByOrderDateBetweenAndStatusIn(
                from, to, List.of(OrderStatus.PENDING, OrderStatus.SHIPPED)
        );

        // ---------------- 매입(전표) 요약 ----------------
        // 1차 구현: POSTED 전표에서
        // - 비용(EXPENSE) 계정의 차변-대변 합계를 '공급가액'으로
        // - "부가세/VAT" 계정의 차변 합계를 'VAT'로 추정
        long monthPurchaseSubtotal = journalItemRepository.sumExpense(from, to);
        long monthPurchaseVat = journalItemRepository.sumInputVat(from, to);
        long monthPurchaseTotal = monthPurchaseSubtotal + monthPurchaseVat;

        return new AccountingSummaryView(
                monthSalesTotal,
                monthPurchaseTotal,
                expectedVat,
                unprocessed
        );
    }

    /**
     * 매출/매입 거래내역 조회
     * - sales   : orders 기반
     * - purchase: journal(전표) 기반 (POSTED)
     * - 전체    : 매출+매입 합쳐 날짜 내림차순 상위 50건
     */
    public List<AccountingTransactionRowView> searchTransactions(AccountingSearchQuery q) {
        String type = (q == null ? null : emptyToNull(q.getType()));
        String partyName = (q == null ? null : emptyToNull(q.getPartyName()));

        LocalDateTime from = (q == null ? null : parseFrom(q.getFrom()));
        LocalDateTime toExclusive = (q == null ? null : parseToExclusive(q.getTo()));

        if ("sales".equalsIgnoreCase(type)) {
            return toViews(searchSalesDatedRows(partyName, from, toExclusive));
        }
        if ("purchase".equalsIgnoreCase(type)) {
            return toViews(searchPurchaseDatedRows(partyName, from, toExclusive));
        }

        // 전체: 합쳐서 날짜순 상위 50
        List<DatedRow> merged = new ArrayList<>();
        merged.addAll(searchSalesDatedRows(partyName, from, toExclusive));
        merged.addAll(searchPurchaseDatedRows(partyName, from, toExclusive));

        merged.sort((a, b) -> {
            LocalDateTime ad = (a == null ? null : a.dateTime());
            LocalDateTime bd = (b == null ? null : b.dateTime());
            if (ad == null && bd == null) return 0;
            if (ad == null) return 1;
            if (bd == null) return -1;
            return bd.compareTo(ad);
        });

        if (merged.size() > 50) {
            merged = merged.subList(0, 50);
        }

        return toViews(merged);
    }

    private static List<AccountingTransactionRowView> toViews(List<DatedRow> dated) {
        List<AccountingTransactionRowView> rows = new ArrayList<>();
        if (dated == null) return rows;
        for (DatedRow dr : dated) {
            if (dr == null || dr.row() == null) continue;
            rows.add(dr.row());
        }
        return rows;
    }

    private List<DatedRow> searchSalesDatedRows(String partyName, LocalDateTime from, LocalDateTime toExclusive) {
        Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "orderDate"));

        List<Order> orders = orderRepository.searchAccounting(
                partyName,
                from,
                toExclusive,
                pageable
        ).getContent();

        List<DatedRow> rows = new ArrayList<>();
        for (Order o : orders) {
            rows.add(new DatedRow(
                    o.getOrderDate(),
                    new AccountingTransactionRowView(
                            "매출",
                            o.getClient().getName(),
                            o.getOrderNo(),
                            o.getOrderDate().toLocalDate().format(DATE_FMT),
                            bdToLong(o.getSubtotalAmount()),
                            bdToLong(o.getTaxAmount()),
                            bdToLong(o.getTotalAmount()),
                            mapStatusLabel(o.getStatus()),
                            mapBadgeClass(o.getStatus())
                    )
            ));
        }

        return rows;
    }

    private List<DatedRow> searchPurchaseDatedRows(String partyName, LocalDateTime from, LocalDateTime toExclusive) {
        // 전표 기반 "매입" (기본: POSTED만)
        Pageable pageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "entryDate"));

        List<JournalEntry> base = journalEntryRepository.search(JournalStatus.POSTED, from, toExclusive, pageable)
                .getContent();

        // paging + fetch join 충돌 회피를 위해: id 목록만 뽑아 2-step 로딩
        List<Long> ids = new ArrayList<>();
        for (JournalEntry je : base) {
            if (je == null || je.getId() == null) continue;
            if (partyName != null && !matchesPurchasePartyName(je, partyName)) continue;

            ids.add(je.getId());
            if (ids.size() >= 50) break;
        }
        if (ids.isEmpty()) return List.of();

        List<JournalEntry> loaded = journalEntryRepository.findWithItemsByIdIn(ids);
        Map<Long, JournalEntry> byId = new HashMap<>();
        for (JournalEntry je : loaded) {
            if (je != null && je.getId() != null) byId.put(je.getId(), je);
        }

        List<DatedRow> rows = new ArrayList<>();
        for (Long id : ids) {
            JournalEntry je = byId.get(id);
            if (je == null) continue;

            AmountTriple amt = computePurchaseAmounts(je);

            LocalDateTime dt = je.getEntryDate();
            String dateStr = (dt == null) ? "" : dt.toLocalDate().format(DATE_FMT);

            rows.add(new DatedRow(
                    dt,
                    new AccountingTransactionRowView(
                            "매입",
                            resolvePurchasePartyName(je),
                            formatJournalNo(je.getId()),
                            dateStr,
                            amt.subtotal(),
                            amt.vat(),
                            amt.total(),
                            mapJournalStatusLabel(je.getStatus()),
                            mapJournalBadgeClass(je.getStatus())
                    )
            ));
        }

        return rows;
    }

    private static boolean matchesPurchasePartyName(JournalEntry je, String partyName) {
        if (partyName == null || partyName.isBlank()) return true;
        String key = partyName.trim().toLowerCase();

        String desc = (je.getDescription() == null ? "" : je.getDescription()).toLowerCase();
        if (!desc.isBlank() && desc.contains(key)) return true;

        String refType = (je.getReferenceType() == null ? "" : je.getReferenceType()).toLowerCase();
        return !refType.isBlank() && refType.contains(key);
    }

    private static String resolvePurchasePartyName(JournalEntry je) {
        if (je == null) return "-";

        // referenceType에 "VENDOR:OOO" 같이 넣는 형태면 자동 파싱
        String rt = emptyToNull(je.getReferenceType());
        if (rt != null) {
            int idx = rt.indexOf(':');
            if (idx >= 0 && idx < rt.length() - 1) {
                String parsed = rt.substring(idx + 1).trim();
                if (!parsed.isBlank()) return parsed;
            }
        }

        String desc = emptyToNull(je.getDescription());
        return (desc == null ? "-" : desc);
    }

    private static String formatJournalNo(Long id) {
        if (id == null) return "JV-000000";
        return "JV-" + String.format("%06d", id);
    }

    private record AmountTriple(long subtotal, long vat, long total) {}

    private static AmountTriple computePurchaseAmounts(JournalEntry je) {
        // 1차 구현 규칙:
        // - 총액 = 전표 라인의 debit 합계
        // - VAT  = VAT 계정(debit)의 합계
        // - 공급가액 = 총액 - VAT
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal vatDebit = BigDecimal.ZERO;

        if (je != null && je.getItems() != null) {
            for (JournalItem item : je.getItems()) {
                if (item == null) continue;
                BigDecimal d = (item.getDebit() == null ? BigDecimal.ZERO : item.getDebit());
                totalDebit = totalDebit.add(d);

                if (isVatAccount(item.getAccount())) {
                    vatDebit = vatDebit.add(d);
                }
            }
        }

        BigDecimal subtotal = totalDebit.subtract(vatDebit);
        return new AmountTriple(
                bdToLong(subtotal),
                bdToLong(vatDebit),
                bdToLong(totalDebit)
        );
    }

    private static boolean isVatAccount(Account account) {
        if (account == null) return false;
        String name = account.getName();
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("부가세") || n.contains("vat");
    }

    private static String mapJournalStatusLabel(JournalStatus status) {
        if (status == null) return "전표";
        return switch (status) {
            case DRAFT -> "작성중";
            case POSTED -> "확정";
            case CLOSED -> "마감";
        };
    }

    private static String mapJournalBadgeClass(JournalStatus status) {
        if (status == null) return "bg-secondary";
        return switch (status) {
            case DRAFT -> "bg-secondary";
            case POSTED -> "bg-success";
            case CLOSED -> "bg-dark";
        };
    }

    private record DatedRow(LocalDateTime dateTime, AccountingTransactionRowView row) {}

    // ---------------- 손익 리포트 (주문 기반 유지) ----------------

    public ProfitReportSummaryView getProfitSummary(ProfitReportQuery pq) {
        Range r = resolveProfitRange(pq);
        List<OrderStatus> statuses = resolveBasisStatuses(pq == null ? null : pq.getBasis());

        BigDecimal subBd = orderRepository.sumSubtotalBetweenStatuses(r.from, r.toExclusive, statuses);
        BigDecimal vatBd = orderRepository.sumTaxBetweenStatuses(r.from, r.toExclusive, statuses);
        BigDecimal totBd = orderRepository.sumTotalBetweenStatuses(r.from, r.toExclusive, statuses);
        BigDecimal profBd = orderRepository.sumProfitBetweenStatuses(r.from, r.toExclusive, statuses);

        long salesSubtotal = bdToLong(subBd);
        long salesVat = bdToLong(vatBd);
        long salesTotal = bdToLong(totBd);
        long grossProfit = bdToLong(profBd);

        long cogs = salesSubtotal - grossProfit;
        double profitRate = (salesSubtotal == 0L) ? 0.0 : (grossProfit * 100.0) / salesSubtotal;

        long orderCount = orderRepository.countByOrderDateBetweenAndStatusIn(r.from, r.toExclusive, statuses);

        return new ProfitReportSummaryView(
                salesSubtotal, salesVat, salesTotal,
                grossProfit, cogs, profitRate, orderCount
        );
    }

    public List<ProfitReportRowView> getProfitRows(ProfitReportQuery pq) {
        Range r = resolveProfitRange(pq);
        List<OrderStatus> statuses = resolveBasisStatuses(pq == null ? null : pq.getBasis());

        List<Object[]> raw = orderRepository.sumProfitByManager(r.from, r.toExclusive, statuses);

        List<ProfitReportRowView> rows = new ArrayList<>();
        for (Object[] row : raw) {
            String label = (String) row[0];
            BigDecimal subBd = (BigDecimal) row[1];
            BigDecimal profBd = (BigDecimal) row[2];

            long salesSubtotal = bdToLong(subBd);
            long grossProfit = bdToLong(profBd);
            double profitRate = (salesSubtotal == 0L) ? 0.0 : (grossProfit * 100.0) / salesSubtotal;

            rows.add(new ProfitReportRowView(label, salesSubtotal, grossProfit, profitRate));
        }
        return rows;
    }

    private static List<OrderStatus> resolveBasisStatuses(String basis) {
        String b = (basis == null) ? "" : basis.trim().toLowerCase();
        return switch (b) {
            case "shipped" -> List.of(OrderStatus.SHIPPED, OrderStatus.SETTLED);
            case "all" -> List.of(OrderStatus.PENDING, OrderStatus.SHIPPED, OrderStatus.SETTLED);
            default -> List.of(OrderStatus.SETTLED);
        };
    }

    private static Range resolveProfitRange(ProfitReportQuery pq) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        LocalDateTime from = null;
        LocalDateTime toEx = null;

        if (pq != null) {
            from = parseFrom(pq.getFrom());
            toEx = parseToExclusive(pq.getTo());
        }

        if (from == null && toEx == null) {
            from = monthStart.atStartOfDay();
            toEx = monthStart.plusMonths(1).atStartOfDay();
        } else if (from != null && toEx == null) {
            toEx = from.toLocalDate().plusMonths(1).atStartOfDay();
        } else if (from == null && toEx != null) {
            LocalDate endInclusive = toEx.toLocalDate().minusDays(1);
            from = endInclusive.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        }

        return new Range(from, toEx);
    }

    private record Range(LocalDateTime from, LocalDateTime toExclusive) {}

    // ---------------- 주문 helper ----------------

    private static String mapStatusLabel(OrderStatus status) {
        return switch (status) {
            case SETTLED -> "결제완료";
            case PENDING -> "진행중";
            case SHIPPED -> "출고완료";
            case CANCELLED -> "취소";
            case RETURNED -> "반품";
        };
    }

    private static String mapBadgeClass(OrderStatus status) {
        return switch (status) {
            case SETTLED -> "bg-success";
            case PENDING -> "bg-warning text-dark";
            case SHIPPED -> "bg-primary";
            case CANCELLED, RETURNED -> "bg-danger";
        };
    }

    private static LocalDateTime parseFrom(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return null;
        LocalDate d = LocalDate.parse(yyyyMmDd, DATE_FMT);
        return d.atStartOfDay();
    }

    private static LocalDateTime parseToExclusive(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isBlank()) return null;
        LocalDate d = LocalDate.parse(yyyyMmDd, DATE_FMT);
        return d.plusDays(1).atStartOfDay();
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static long bdToLong(BigDecimal bd) {
        if (bd == null) return 0L;
        return bd.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
