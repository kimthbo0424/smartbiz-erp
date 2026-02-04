package com.smartbiz.erp.accounting.dto;

public class IncomeStatementView {

    private final long revenue;
    private final long expense;
    private final long operatingProfit;

    public IncomeStatementView(long revenue, long expense, long operatingProfit) {
        this.revenue = revenue;
        this.expense = expense;
        this.operatingProfit = operatingProfit;
    }

    public IncomeStatementView(long revenue, long expense) {
        this(revenue, expense, revenue - expense);
    }

    public long getRevenue() { return revenue; }
    public long getExpense() { return expense; }
    public long getOperatingProfit() { return operatingProfit; }
}
