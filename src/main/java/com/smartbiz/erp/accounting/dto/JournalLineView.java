package com.smartbiz.erp.accounting.dto;

public class JournalLineView {
    private final String accountName;
    private final long debit;
    private final long credit;
    private final String description;

    public JournalLineView(String accountName, long debit, long credit, String description) {
        this.accountName = accountName;
        this.debit = debit;
        this.credit = credit;
        this.description = description;
    }

    public String getAccountName() { return accountName; }
    public long getDebit() { return debit; }
    public long getCredit() { return credit; }
    public String getDescription() { return description; }
}
