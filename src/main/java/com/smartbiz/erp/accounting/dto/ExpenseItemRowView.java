package com.smartbiz.erp.accounting.dto;

public class ExpenseItemRowView {

    private final Long id;
    private final String name;
    private final String cycle;       // enum name (MONTHLY/ONCE)
    private final String cycleLabel;  // "월별"/"일시"
    private final boolean active;

    public ExpenseItemRowView(Long id, String name, String cycle, String cycleLabel, boolean active) {
        this.id = id;
        this.name = name;
        this.cycle = cycle;
        this.cycleLabel = cycleLabel;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCycle() { return cycle; }
    public String getCycleLabel() { return cycleLabel; }
    public boolean isActive() { return active; }
    public boolean getActive() { return active; } // thymeleaf 호환용
}
