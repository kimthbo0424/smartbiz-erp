package com.smartbiz.erp.accounting.dto;

public class JournalLineForm {
    private Integer accountId;
    private long debit;
    private long credit;
    private String description;

    public Integer getAccountId() { return accountId; }
    public long getDebit() { return debit; }
    public long getCredit() { return credit; }
    public String getDescription() { return description; }

    public void setAccountId(Integer accountId) { this.accountId = accountId; }
    public void setDebit(long debit) { this.debit = debit; }
    public void setCredit(long credit) { this.credit = credit; }
    public void setDescription(String description) { this.description = description; }
}
