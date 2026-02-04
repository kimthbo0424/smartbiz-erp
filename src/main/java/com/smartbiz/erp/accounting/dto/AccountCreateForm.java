// src/main/java/com/smartbiz/erp/accounting/dto/AccountCreateForm.java
package com.smartbiz.erp.accounting.dto;

public class AccountCreateForm {
    private Integer id;        // 계정 코드(예: 1000) - 선택(프로젝트 정책에 따라 필수로 바꿔도 됨)
    private String name;       // 계정명
    private String type;       // enum name 또는 문자열
    private Integer parentId;  // 상위계정(선택)
    private Boolean active;    // 선택(엔티티에 active가 없으면 무시됨)

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public Integer getParentId() { return parentId; }
    public Boolean getActive() { return active; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setActive(Boolean active) { this.active = active; }
}
