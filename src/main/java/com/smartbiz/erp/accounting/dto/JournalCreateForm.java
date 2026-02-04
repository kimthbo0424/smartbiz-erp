package com.smartbiz.erp.accounting.dto;

import java.util.ArrayList;
import java.util.List;

public class JournalCreateForm {
    private String entryDate;     // yyyy-MM-dd
    private String description;
    private String referenceType; // optional
    private Long referenceId;     // optional
    private List<JournalLineForm> lines = new ArrayList<>();

    public String getEntryDate() { return entryDate; }
    public String getDescription() { return description; }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public List<JournalLineForm> getLines() { return lines; }

    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public void setDescription(String description) { this.description = description; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public void setLines(List<JournalLineForm> lines) { this.lines = lines; }
}
