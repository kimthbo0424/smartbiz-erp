package com.smartbiz.erp.accounting.dto;

public class VatRowView {

    private final String date;
    private final String partyName;
    private final String voucherNo;
    private final long subtotal;
    private final long vat;
    private final long total;
    private final String statusLabel;
    private final String badgeClass;

    public VatRowView(String date,
                      String partyName,
                      String voucherNo,
                      long subtotal,
                      long vat,
                      long total,
                      String statusLabel,
                      String badgeClass) {
        this.date = date;
        this.partyName = partyName;
        this.voucherNo = voucherNo;
        this.subtotal = subtotal;
        this.vat = vat;
        this.total = total;
        this.statusLabel = statusLabel;
        this.badgeClass = badgeClass;
    }

    public String getDate() { return date; }
    public String getPartyName() { return partyName; }
    public String getVoucherNo() { return voucherNo; }
    public long getSubtotal() { return subtotal; }
    public long getVat() { return vat; }
    public long getTotal() { return total; }
    public String getStatusLabel() { return statusLabel; }
    public String getBadgeClass() { return badgeClass; }
}
