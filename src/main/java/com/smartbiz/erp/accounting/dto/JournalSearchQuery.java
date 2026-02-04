package com.smartbiz.erp.accounting.dto;

public class JournalSearchQuery {
    private String status; // DRAFT / POSTED / CLOSED
    private String from;   // yyyy-MM-dd
    private String to;     // yyyy-MM-dd

    public String getStatus() { return status; }
    public String getFrom() { return from; }
    public String getTo() { return to; }

    public void setStatus(String status) { this.status = status; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
}
