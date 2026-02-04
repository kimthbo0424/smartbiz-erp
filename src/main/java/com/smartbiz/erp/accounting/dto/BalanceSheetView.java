package com.smartbiz.erp.accounting.dto;

public class BalanceSheetView {

    private final long assets;
    private final long liabilities;
    private final long equity;

    // 기존에 (assets, liabilities, equity) 생성자가 있었다고 가정하고 유지
    public BalanceSheetView(long assets, long liabilities, long equity) {
        this.assets = assets;
        this.liabilities = liabilities;
        this.equity = equity;
    }

    // ✅ 컴파일 에러 해결용 오버로드 (assets, liabilities)만 받아도 되게 추가
    // 기존 의미를 바꾸지 않도록 equity는 기본적으로 assets - liabilities로 계산
    public BalanceSheetView(long assets, long liabilities) {
        this(assets, liabilities, assets - liabilities);
    }

    public long getAssets() { return assets; }
    public long getLiabilities() { return liabilities; }
    public long getEquity() { return equity; }
}
