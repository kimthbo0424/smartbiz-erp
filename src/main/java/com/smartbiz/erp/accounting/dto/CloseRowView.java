package com.smartbiz.erp.accounting.dto;

import com.smartbiz.erp.accounting.domain.AccountingClose;
import com.smartbiz.erp.accounting.domain.AccountingCloseStatus;

import java.time.format.DateTimeFormatter;

public class CloseRowView {

    private final Long id;
    private final String typeLabel;
    private final String periodKey;
    private final String closedTo;
    private final String closedAt;
    private final String closedBy;
    private final String statusLabel;
    private final String badgeClass;

    public CloseRowView(Long id,
                        String typeLabel,
                        String periodKey,
                        String closedTo,
                        String closedAt,
                        String closedBy,
                        String statusLabel,
                        String badgeClass) {
        this.id = id;
        this.typeLabel = typeLabel;
        this.periodKey = periodKey;
        this.closedTo = closedTo;
        this.closedAt = closedAt;
        this.closedBy = closedBy;
        this.statusLabel = statusLabel;
        this.badgeClass = badgeClass;
    }

    public static CloseRowView from(AccountingClose c) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String typeLabel = switch (c.getType()) {
            case MONTH -> "월 마감";
            case QUARTER -> "분기 마감";
            case YEAR -> "연 마감";
        };

        boolean closed = c.getStatus() == AccountingCloseStatus.CLOSED;
        String badge = closed ? " bg-danger" : " bg-secondary";
        String statusLabel = closed ? "마감" : "역마감";

        return new CloseRowView(
                c.getId(),
                typeLabel,
                c.getPeriodKey(),
                c.getClosedTo() != null ? c.getClosedTo().toString() : "",
                c.getClosedAt() != null ? c.getClosedAt().format(dt) : "",
                c.getClosedBy() != null ? c.getClosedBy() : "-",
                statusLabel,
                badge
        );
    }

    public Long getId() { return id; }
    public String getTypeLabel() { return typeLabel; }
    public String getPeriodKey() { return periodKey; }
    public String getClosedTo() { return closedTo; }
    public String getClosedAt() { return closedAt; }
    public String getClosedBy() { return closedBy; }
    public String getStatusLabel() { return statusLabel; }
    public String getBadgeClass() { return badgeClass; }
}
