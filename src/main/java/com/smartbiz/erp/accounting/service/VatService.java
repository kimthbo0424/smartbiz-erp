// src/main/java/com/smartbiz/erp/accounting/service/VatService.java
package com.smartbiz.erp.accounting.service;

import com.smartbiz.erp.accounting.dto.VatReportQuery;
import com.smartbiz.erp.accounting.dto.VatRowView;
import com.smartbiz.erp.accounting.dto.VatSummaryView;
import com.smartbiz.erp.orders.domain.Order;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class VatService {

    private final OrderRepository orderRepository;

    public VatService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public VatSummaryView getSummary(VatReportQuery q) {
        YearMonth ym = parseMonthOrNow(q.getMonth());
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime toExclusive = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderStatus> excluded = buildExcludedStatuses();

        long salesSubtotal = safeLong(orderRepository.sumSalesSubtotalForVat(from, toExclusive, excluded));
        long salesVat = safeLong(orderRepository.sumSalesVatForVat(from, toExclusive, excluded));
        long salesTotal = safeLong(orderRepository.sumSalesTotalForVat(from, toExclusive, excluded));
        int salesCount = (int) orderRepository.countSalesForVat(from, toExclusive, excluded);

        long purchaseSubtotal = 0L; // 1차: 미구현
        long purchaseVat = 0L;      // 1차: 미구현(추후 전표 기반 매입세액 연결)

        return new VatSummaryView(ym.toString(), salesSubtotal, salesVat, salesTotal, salesCount, purchaseSubtotal, purchaseVat);
    }

    public List<VatRowView> getRows(VatReportQuery q) {
        YearMonth ym = parseMonthOrNow(q.getMonth());
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime toExclusive = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderStatus> excluded = buildExcludedStatuses();

        List<Order> orders = orderRepository.findSalesOrdersForVat(
                from,
                toExclusive,
                excluded,
                PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        List<VatRowView> rows = new ArrayList<>();
        for (Order o : orders) {
            String date = (o.getOrderDate() == null) ? "" : o.getOrderDate().toLocalDate().toString();
            String party = (o.getClient() == null) ? "" : o.getClient().getName();

            long subtotal = safeLong(o.getSubtotalAmount());
            long vat = safeLong(o.getTaxAmount());
            long total = safeLong(o.getTotalAmount());

            String statusLabel = mapStatusLabel(o.getStatus());
            String badgeClass = mapBadgeClass(o.getStatus());

            rows.add(new VatRowView(date, party, o.getOrderNo(), subtotal, vat, total, statusLabel, badgeClass));
        }
        return rows;
    }

    private YearMonth parseMonthOrNow(String yyyyMm) {
        try {
            if (yyyyMm == null || yyyyMm.isBlank()) return YearMonth.now();
            return YearMonth.parse(yyyyMm.trim());
        } catch (Exception e) {
            return YearMonth.now();
        }
    }

    private List<OrderStatus> buildExcludedStatuses() {
        List<OrderStatus> list = new ArrayList<>();
        try { list.add(OrderStatus.valueOf("CANCELLED")); } catch (Exception ignored) {}
        try { list.add(OrderStatus.valueOf("CANCELED")); } catch (Exception ignored) {}
        try { list.add(OrderStatus.valueOf("RETURNED")); } catch (Exception ignored) {}
        return list;
    }

    private long safeLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof BigDecimal bd) return bd.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    private String mapStatusLabel(OrderStatus s) {
        if (s == null) return "";
        return switch (s) {
            case PENDING -> "대기";
            case SHIPPED -> "출고";
            case SETTLED -> "정산";
            case CANCELLED -> "취소";
            case RETURNED -> "반품";
        };
    }

    private String mapBadgeClass(OrderStatus s) {
        if (s == null) return "bg-secondary";
        return switch (s) {
            case PENDING -> "bg-secondary";
            case SHIPPED -> "bg-primary";
            case SETTLED -> "bg-success";
            case CANCELLED, RETURNED -> "bg-danger";
        };
    }
}
