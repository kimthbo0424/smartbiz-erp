package com.smartbiz.erp.accounting.dto;

public class AccountingTransactionRowView {
    private final String typeLabel;     // 매출/매입
    private final String partyName;     // 거래처
    private final String voucherNo;     // 전표번호(초기: orderNo 사용)
    private final String date;          // yyyy-MM-dd
    private final long subtotal;        // 공급가액
    private final long vat;             // 부가세
    private final long total;           // 총액
    private final String statusLabel;   // 결제/상태 라벨
    private final String badgeClass;    // 부트스트랩 badge class (bg-success 등)

    public AccountingTransactionRowView(String typeLabel, String partyName, String voucherNo, String date,
                                        long subtotal, long vat, long total,
                                        String statusLabel, String badgeClass) {
        this.typeLabel = typeLabel;
        this.partyName = partyName;
        this.voucherNo = voucherNo;
        this.date = date;
        this.subtotal = subtotal;
        this.vat = vat;
        this.total = total;
        this.statusLabel = statusLabel;
        this.badgeClass = badgeClass;
    }

    public String getTypeLabel() { return typeLabel; }
    public String getPartyName() { return partyName; }
    public String getVoucherNo() { return voucherNo; }
    public String getDate() { return date; }
    public long getSubtotal() { return subtotal; }
    public long getVat() { return vat; }
    public long getTotal() { return total; }
    public String getStatusLabel() { return statusLabel; }
    public String getBadgeClass() { return badgeClass; }
}
