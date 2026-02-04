// src/main/java/com/smartbiz/erp/accounting/dto/AccountTypeOptionView.java
package com.smartbiz.erp.accounting.dto;

public class AccountTypeOptionView {
    private final String value;
    private final String label;

    public AccountTypeOptionView(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }
}
