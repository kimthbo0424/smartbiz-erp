package com.smartbiz.erp.accounting.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journalentry") // ✅ DB 실제 테이블명
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_id")
    private Long id;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JournalStatus status = JournalStatus.DRAFT;

    // employee 테이블 FK를 아직 도메인으로 안 끌고왔으면 Long으로 유지
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalItem> items = new ArrayList<>();

    public void addItem(JournalItem item) {
        if (item == null) return;
        item.setJournal(this);
        this.items.add(item);
    }

    public void clearItems() {
        for (JournalItem item : items) {
            item.setJournal(null);
        }
        items.clear();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public JournalStatus getStatus() { return status; }
    public void setStatus(JournalStatus status) { this.status = status; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public List<JournalItem> getItems() { return items; }
    public void setItems(List<JournalItem> items) { this.items = items; }
}
