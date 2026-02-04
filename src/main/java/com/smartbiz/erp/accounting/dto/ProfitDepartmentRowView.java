package com.smartbiz.erp.accounting.dto;

public class ProfitDepartmentRowView {
    private final String deptName;
    private final long sales;   // 매출(공급가액)
    private final long cost;    // 비용(근사)
    private final long profit;  // 영업이익

    public ProfitDepartmentRowView(String deptName, long sales, long cost, long profit) {
        this.deptName = deptName;
        this.sales = sales;
        this.cost = cost;
        this.profit = profit;
    }

    public String getDeptName() { return deptName; }
    public long getSales() { return sales; }
    public long getCost() { return cost; }
    public long getProfit() { return profit; }
}
