package com.smartbiz.erp.support;

public enum SupportStatus {
    DRAFT("접수 전"),
    RECEIVED("접수"),
    ANSWERED("처리 완료");

    private final String label;

    SupportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
