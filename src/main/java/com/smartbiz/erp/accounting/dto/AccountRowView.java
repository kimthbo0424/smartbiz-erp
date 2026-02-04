// src/main/java/com/smartbiz/erp/accounting/dto/AccountRowView.java
package com.smartbiz.erp.accounting.dto;

public class AccountRowView {
    private final int id;
    private final String name;
    private final String type;       // 원본 값(enum name 등)
    private final String typeLabel;  // 화면 표시용
    private final Integer parentId;
    private final String parentName;
    private final boolean active;

    public AccountRowView(int id, String name, String type, String typeLabel,
                          Integer parentId, String parentName,
                          boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.typeLabel = typeLabel;
        this.parentId = parentId;
        this.parentName = parentName;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getTypeLabel() { return typeLabel; }
    public Integer getParentId() { return parentId; }
    public String getParentName() { return parentName; }
    public boolean isActive() { return active; }
}
