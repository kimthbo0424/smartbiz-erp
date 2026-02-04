package com.smartbiz.erp.notice;

public enum NoticeStatus {
    PUBLISHED("게시"),
    DRAFT("임시"),
    HIDDEN("숨김");

    private final String label;

    NoticeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
