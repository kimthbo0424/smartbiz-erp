package com.smartbiz.erp.accounting.dto;

import java.util.List;

public class JournalDetailView {
    private final long id;
    private final String journalNo;
    private final String entryDate;     // yyyy-MM-dd
    private final String description;
    private final String status;        // DRAFT/POSTED/CLOSED
    private final String statusLabel;
    private final String badgeClass;
    private final long debitTotal;
    private final long creditTotal;
    private final List<JournalLineView> lines;

    public JournalDetailView(long id, String journalNo, String entryDate, String description,
                             String status, String statusLabel, String badgeClass,
                             long debitTotal, long creditTotal,
                             List<JournalLineView> lines) {
        this.id = id;
        this.journalNo = journalNo;
        this.entryDate = entryDate;
        this.description = description;
        this.status = status;
        this.statusLabel = statusLabel;
        this.badgeClass = badgeClass;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
        this.lines = lines;
    }

    public long getId() { return id; }
    public String getJournalNo() { return journalNo; }
    public String getEntryDate() { return entryDate; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public String getBadgeClass() { return badgeClass; }
    public long getDebitTotal() { return debitTotal; }
    public long getCreditTotal() { return creditTotal; }
    public List<JournalLineView> getLines() { return lines; }
}
