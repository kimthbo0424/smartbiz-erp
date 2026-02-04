// src/main/java/com/smartbiz/erp/accounting/dto/ExpenseItemCreateForm.java
package com.smartbiz.erp.accounting.dto;

import com.smartbiz.erp.accounting.domain.ExpenseCycle;

public class ExpenseItemCreateForm {
    private String name;
    private ExpenseCycle cycle; // MONTHLY / ONE_TIME / VARIABLE
    private Boolean active;

    public String getName() { return name; }
    public ExpenseCycle getCycle() { return cycle; }
    public Boolean getActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setCycle(ExpenseCycle cycle) { this.cycle = cycle; }
    public void setActive(Boolean active) { this.active = active; }
}
