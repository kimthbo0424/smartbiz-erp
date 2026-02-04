package com.smartbiz.erp.reports.dto;

import java.util.List;

public class ReportView {

    private final long totalSales;
    private final long totalCosts;   // ✅ 지출
    private final long netProfit;    // ✅ 매출-지출

    private final long orderCount;
    private final double momGrowthRate;

    private final String profitTrendJson; // labels, sales, costs
    private final String top10Json;       // top products
    private final String topExpenseJson;  // ✅ top expense accounts

    private final List<TopProductRow> topProducts;
    private final List<PartyRow> partyRows;

    private final List<TopExpenseRow> topExpenses;        // ✅ Top 비용계정
    private final List<ExpenseLineRow> expenseLines;      // ✅ 최근 지출내역

    public ReportView(long totalSales,
                      long totalCosts,
                      long netProfit,
                      long orderCount,
                      double momGrowthRate,
                      String profitTrendJson,
                      String top10Json,
                      String topExpenseJson,
                      List<TopProductRow> topProducts,
                      List<PartyRow> partyRows,
                      List<TopExpenseRow> topExpenses,
                      List<ExpenseLineRow> expenseLines) {
        this.totalSales = totalSales;
        this.totalCosts = totalCosts;
        this.netProfit = netProfit;
        this.orderCount = orderCount;
        this.momGrowthRate = momGrowthRate;
        this.profitTrendJson = profitTrendJson;
        this.top10Json = top10Json;
        this.topExpenseJson = topExpenseJson;
        this.topProducts = topProducts;
        this.partyRows = partyRows;
        this.topExpenses = topExpenses;
        this.expenseLines = expenseLines;
    }

    public long getTotalSales() { return totalSales; }
    public long getTotalCosts() { return totalCosts; }
    public long getNetProfit() { return netProfit; }

    public long getOrderCount() { return orderCount; }
    public double getMomGrowthRate() { return momGrowthRate; }

    public String getProfitTrendJson() { return profitTrendJson; }
    public String getTop10Json() { return top10Json; }
    public String getTopExpenseJson() { return topExpenseJson; }

    public List<TopProductRow> getTopProducts() { return topProducts; }
    public List<PartyRow> getPartyRows() { return partyRows; }

    public List<TopExpenseRow> getTopExpenses() { return topExpenses; }
    public List<ExpenseLineRow> getExpenseLines() { return expenseLines; }

    // -----------------------------
    // Rows
    // -----------------------------
    public static class TopProductRow {
        private final String name;
        private final long sales;

        public TopProductRow(String name, long sales) {
            this.name = name;
            this.sales = sales;
        }
        public String getName() { return name; }
        public long getSales() { return sales; }
    }

    public static class PartyRow {
        private final String party;
        private final long sales;

        public PartyRow(String party, long sales) {
            this.party = party;
            this.sales = sales;
        }
        public String getParty() { return party; }
        public long getSales() { return sales; }
    }

    // ✅ Top 비용계정
    public static class TopExpenseRow {
        private final String name;
        private final long amount;

        public TopExpenseRow(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }
        public String getName() { return name; }
        public long getAmount() { return amount; }
    }

    // ✅ 최근 지출내역
    public static class ExpenseLineRow {
        private final String date;       // yyyy-MM-dd
        private final String voucherNo;  // JV-000001
        private final String accountName;
        private final String description;
        private final long amount;

        public ExpenseLineRow(String date,
                              String voucherNo,
                              String accountName,
                              String description,
                              long amount) {
            this.date = date;
            this.voucherNo = voucherNo;
            this.accountName = accountName;
            this.description = description;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public String getVoucherNo() { return voucherNo; }
        public String getAccountName() { return accountName; }
        public String getDescription() { return description; }
        public long getAmount() { return amount; }
    }
}
