package com.smartbiz.erp.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "journalitem")
public class JournalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private JournalEntry journal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "debit", nullable = false, precision = 15, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(name = "credit", nullable = false, precision = 15, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(name = "description", length = 200)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public JournalEntry getJournal() { return journal; }
    public void setJournal(JournalEntry journal) { this.journal = journal; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = (debit == null ? BigDecimal.ZERO : debit); }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = (credit == null ? BigDecimal.ZERO : credit); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
