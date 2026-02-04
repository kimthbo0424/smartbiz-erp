package com.smartbiz.erp.common;

public class PageView {
    private final int current;
    private final int total;

    public PageView(int current, int total) {
        this.current = current;
        this.total = total;
    }

    public int getCurrent() { return current; }
    public int getTotal() { return total; }
}
