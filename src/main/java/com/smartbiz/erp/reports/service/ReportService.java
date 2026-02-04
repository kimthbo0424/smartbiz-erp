package com.smartbiz.erp.reports.service;

import com.smartbiz.erp.accounting.repository.JournalItemRepository;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.dto.report.ProductSalesRowView; // ✅ dto.report (클래스)
import com.smartbiz.erp.orders.repository.OrderItemRepository;
import com.smartbiz.erp.orders.repository.OrderRepository;
import com.smartbiz.erp.reports.dto.ReportQuery;
import com.smartbiz.erp.reports.dto.ReportView;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final JournalItemRepository journalItemRepository;

    public ReportService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         JournalItemRepository journalItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.journalItemRepository = journalItemRepository;
    }

    public ReportView build(ReportQuery rq) {
        // 기본 기간: 최근 6개월
        YearMonth to = parseMonthOrNow(rq.getToMonth());
        YearMonth from = parseMonthOrDefault(rq.getFromMonth(), to.minusMonths(5));

        LocalDateTime fromDt = from.atDay(1).atStartOfDay();
        LocalDateTime toDtExclusive = to.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderStatus> excluded = List.of(OrderStatus.CANCELLED, OrderStatus.RETURNED);

        long totalSales = bdToLong(orderRepository.sumTotalByPeriod(fromDt, toDtExclusive, excluded));
        long orderCount = orderRepository.countByPeriod(fromDt, toDtExclusive, excluded);

        // 월별 매출/지출 맵 (기간 전체를 0으로 초기화)
        Map<YearMonth, Long> monthSalesMap = new LinkedHashMap<>();
        Map<YearMonth, Long> monthCostsMap = new LinkedHashMap<>();
        for (YearMonth ym : iterateMonths(from, to)) {
            monthSalesMap.put(ym, 0L);
            monthCostsMap.put(ym, 0L);
        }

        // 월별 매출 반영 (orders)
        List<Object[]> salesAgg = orderRepository.sumTotalGroupByMonth(fromDt, toDtExclusive, excluded);
        for (Object[] r : salesAgg) {
            int y = toInt(r[0]);
            int m = toInt(r[1]);
            long sum = bdToLong((BigDecimal) r[2]);
            YearMonth ym = YearMonth.of(y, m);
            if (monthSalesMap.containsKey(ym)) {
                monthSalesMap.put(ym, Math.max(0L, sum));
            }
        }

        // 월별 지출 반영 (POSTED 전표 + EXPENSE)
        List<Object[]> costAgg = journalItemRepository.sumExpenseGroupByMonth(fromDt, toDtExclusive);
        for (Object[] r : costAgg) {
            int y = toInt(r[0]);
            int m = toInt(r[1]);
            long sum = toLong(r[2]);
            YearMonth ym = YearMonth.of(y, m);
            if (monthCostsMap.containsKey(ym)) {
                monthCostsMap.put(ym, Math.max(0L, sum));
            }
        }

        long totalCosts = monthCostsMap.values().stream().mapToLong(Long::longValue).sum();
        long netProfit = totalSales - totalCosts;

        // 전월대비 성장률 (매출 기준)
        double momGrowth = calcMomGrowthRate(to, monthSalesMap);

        // ✅ Top10 상품 (ProductSalesRowView.getTotalAmount()는 long)
        List<ProductSalesRowView> topProductAgg =
                orderItemRepository.sumSalesGroupByProduct(fromDt, toDtExclusive, excluded, PageRequest.of(0, 10));
        List<ReportView.TopProductRow> topProducts = new ArrayList<>();
        for (ProductSalesRowView r : topProductAgg) {
            topProducts.add(new ReportView.TopProductRow(r.getProductName(), r.getTotalAmount()));
        }

        // Top10 거래처
        List<Object[]> topPartyAgg =
                orderRepository.sumTotalGroupByClient(fromDt, toDtExclusive, excluded, PageRequest.of(0, 10));
        List<ReportView.PartyRow> partyRows = new ArrayList<>();
        for (Object[] r : topPartyAgg) {
            String clientName = (String) r[0];
            long sum = bdToLong((BigDecimal) r[1]);
            partyRows.add(new ReportView.PartyRow(clientName, sum));
        }

        // Top10 지출(비용계정)
        List<Object[]> topExpenseAgg =
                journalItemRepository.sumExpenseByAccount(fromDt, toDtExclusive, PageRequest.of(0, 10));
        List<ReportView.TopExpenseRow> topExpenses = new ArrayList<>();
        for (Object[] r : topExpenseAgg) {
            String name = (String) r[0];
            long amt = Math.max(0L, toLong(r[1]));
            topExpenses.add(new ReportView.TopExpenseRow(name, amt));
        }

        // 최근 지출 내역(최근 30건)
        List<Object[]> lineAgg =
                journalItemRepository.findRecentExpenseLines(fromDt, toDtExclusive, PageRequest.of(0, 30));
        List<ReportView.ExpenseLineRow> expenseLines = new ArrayList<>();
        for (Object[] r : lineAgg) {
            Long journalId = (Long) r[0];
            LocalDateTime entryDate = (LocalDateTime) r[1];
            String journalDesc = (String) r[2];
            String accountName = (String) r[3];
            String lineDesc = (String) r[4];
            long amt = Math.max(0L, toLong(r[5]));

            String date = (entryDate == null) ? "" : entryDate.toLocalDate().toString();
            String voucherNo = formatJournalNo(journalId);

            String desc;
            if (lineDesc != null && !lineDesc.isBlank()) desc = lineDesc;
            else if (journalDesc != null) desc = journalDesc;
            else desc = "";

            expenseLines.add(new ReportView.ExpenseLineRow(date, voucherNo, accountName, desc, amt));
        }

        // Chart.js JSON
        String profitTrendJson = buildProfitTrendJson(monthSalesMap, monthCostsMap);
        String top10Json = buildTop10Json(topProducts);
        String topExpenseJson = buildTopExpenseJson(topExpenses);

        return new ReportView(
                totalSales,
                totalCosts,
                netProfit,
                orderCount,
                momGrowth,
                profitTrendJson,
                top10Json,
                topExpenseJson,
                topProducts,
                partyRows,
                topExpenses,
                expenseLines
        );
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private YearMonth parseMonthOrNow(String s) {
        if (s == null || s.isBlank()) return YearMonth.now();
        return YearMonth.parse(s.trim());
    }

    private YearMonth parseMonthOrDefault(String s, YearMonth def) {
        if (s == null || s.isBlank()) return def;
        return YearMonth.parse(s.trim());
    }

    private List<YearMonth> iterateMonths(YearMonth from, YearMonth to) {
        List<YearMonth> out = new ArrayList<>();
        YearMonth cur = from;
        while (!cur.isAfter(to)) {
            out.add(cur);
            cur = cur.plusMonths(1);
        }
        return out;
    }

    private long bdToLong(BigDecimal v) {
        if (v == null) return 0L;
        return v.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Integer i) return i;
        if (o instanceof Long l) return l.intValue();
        if (o instanceof BigDecimal bd) return bd.intValue();
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long l) return l;
        if (o instanceof Integer i) return i.longValue();
        if (o instanceof BigDecimal bd) return bd.setScale(0, RoundingMode.HALF_UP).longValue();
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }

    private double calcMomGrowthRate(YearMonth to, Map<YearMonth, Long> monthSalesMap) {
        long last = monthSalesMap.getOrDefault(to, 0L);
        long prev = monthSalesMap.getOrDefault(to.minusMonths(1), 0L);

        if (prev <= 0L) return (last > 0L ? 100.0 : 0.0);
        return ((double) (last - prev) / (double) prev) * 100.0;
    }

    private String buildProfitTrendJson(Map<YearMonth, Long> sales, Map<YearMonth, Long> costs) {
        List<String> labels = new ArrayList<>();
        List<Long> salesArr = new ArrayList<>();
        List<Long> costArr = new ArrayList<>();

        for (YearMonth ym : sales.keySet()) {
            labels.add(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            salesArr.add(sales.getOrDefault(ym, 0L));
            costArr.add(costs.getOrDefault(ym, 0L));
        }

        return "{"
                + "\"labels\":" + toJsonStringArray(labels) + ","
                + "\"sales\":" + toJsonNumberArray(salesArr) + ","
                + "\"costs\":" + toJsonNumberArray(costArr)
                + "}";
    }

    private String buildTop10Json(List<ReportView.TopProductRow> rows) {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (ReportView.TopProductRow r : rows) {
            labels.add(r.getName());
            values.add(r.getSales());
        }
        return "{"
                + "\"labels\":" + toJsonStringArray(labels) + ","
                + "\"values\":" + toJsonNumberArray(values)
                + "}";
    }

    private String buildTopExpenseJson(List<ReportView.TopExpenseRow> rows) {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (ReportView.TopExpenseRow r : rows) {
            labels.add(r.getName());
            values.add(r.getAmount());
        }
        return "{"
                + "\"labels\":" + toJsonStringArray(labels) + ","
                + "\"values\":" + toJsonNumberArray(values)
                + "}";
    }

    private String toJsonStringArray(List<String> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(arr.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonNumberArray(List<Long> arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(arr.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private String formatJournalNo(Long id) {
        if (id == null) return "JV-000000";
        return "JV-" + String.format("%06d", id);
    }
}
