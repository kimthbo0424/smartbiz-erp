// src/main/java/com/smartbiz/erp/accounting/dto/JournalEntryRowView.java
package com.smartbiz.erp.accounting.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class JournalEntryRowView {

    // props에 id, journalId 둘 다 들어오는 케이스 대비
    private Long id;
    private Long journalId;

    private String journalNo;

    // props에 entryDate(LocalDate) 또는 date(String)가 섞여 들어오는 케이스 대비
    private LocalDate entryDate;
    private String date;

    private String description;

    // status(enum/string) + statusLabel(한글) 둘 다 오는 케이스 대비
    private String status;
    private String statusLabel;

    private String badgeClass;

    private String referenceType;
    private Long referenceId;

    // 합계(쿼리에서 BigDecimal/Long 등으로 올 수 있어 Number로 수용)
    private long debitTotal;
    private long creditTotal;

    // --- 기본 생성자 (리플렉션 setter 주입용) ---
    public JournalEntryRowView() {
    }

    // --- (선택) 기존 코드에서 new 로 만들 가능성 대비 ---
    public JournalEntryRowView(Long id,
                               String journalNo,
                               String date,
                               String description,
                               long debitTotal,
                               long creditTotal,
                               String statusLabel,
                               String badgeClass) {
        setId(id);
        this.journalNo = journalNo;
        setDate(date);
        this.description = description;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
        this.statusLabel = statusLabel;
        this.badgeClass = badgeClass;
    }

    // ---------------- getters ----------------

    public Long getId() {
        return id;
    }

    public Long getJournalId() {
        return journalId;
    }

    public String getJournalNo() {
        return journalNo;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public long getDebitTotal() {
        return debitTotal;
    }

    public long getCreditTotal() {
        return creditTotal;
    }

    // ---------------- setters (리플렉션 주입 호환) ----------------

    public void setId(Long id) {
        this.id = id;
        if (this.journalId == null) {
            this.journalId = id;
        }
    }

    public void setJournalId(Long journalId) {
        this.journalId = journalId;
        if (this.id == null) {
            this.id = journalId;
        }
    }

    public void setJournalNo(String journalNo) {
        this.journalNo = journalNo;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
        if (this.date == null && entryDate != null) {
            this.date = entryDate.toString();
        }
    }

    // props가 String으로 들어오는 경우 대비(예: "2026-01-06")
    public void setEntryDate(String entryDate) {
        if (entryDate == null || entryDate.isBlank()) return;
        try {
            setEntryDate(LocalDate.parse(entryDate.trim()));
        } catch (Exception e) {
            // 파싱 실패 시에는 표시용 date만 세팅
            this.date = entryDate;
        }
    }

    public void setDate(String date) {
        this.date = date;
        if (this.entryDate == null && date != null && !date.isBlank()) {
            try {
                this.entryDate = LocalDate.parse(date.trim());
            } catch (Exception ignored) {
            }
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // enum이든 String이든 결국 toString()로 들어오게 수용
    public void setStatus(Object status) {
        this.status = (status == null) ? null : String.valueOf(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public void setReferenceId(Number referenceId) {
        this.referenceId = (referenceId == null) ? null : referenceId.longValue();
    }

    public void setDebitTotal(Number debitTotal) {
        this.debitTotal = numberToLong(debitTotal);
    }

    public void setCreditTotal(Number creditTotal) {
        this.creditTotal = numberToLong(creditTotal);
    }

    private static long numberToLong(Number n) {
        if (n == null) return 0L;
        if (n instanceof BigDecimal bd) {
            return bd.setScale(0, RoundingMode.HALF_UP).longValue();
        }
        return n.longValue();
    }
}
