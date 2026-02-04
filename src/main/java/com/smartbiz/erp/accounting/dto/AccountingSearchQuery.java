package com.smartbiz.erp.accounting.dto;

public class AccountingSearchQuery {
    private String partyName; // 거래처명
    private String type;      // sales / purchase (초기엔 sales만)
    private String from;      // yyyy-MM-dd
    private String to;        // yyyy-MM-dd

    public String getPartyName() { return partyName; }
    public String getType() { return type; }
    public String getFrom() { return from; }
    public String getTo() { return to; }

    public void setPartyName(String partyName) { this.partyName = partyName; }
    public void setType(String type) { this.type = type; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
}
