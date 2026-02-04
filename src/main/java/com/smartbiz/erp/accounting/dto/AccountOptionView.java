package com.smartbiz.erp.accounting.dto;

/**
 * 계정과목 선택용 DTO
 *
 * JournalService의 리플렉션 기반 DTO 생성 로직이
 * 다음 props 조합을 사용할 수 있으므로 모두 수용한다:
 * - accountId, name, id, label, type
 *
 * ✅ 지원 방식
 * 1) 기본 생성자 + setter 주입
 * 2) 생성자 매칭 (특히 5-arg: accountId, name, id, label, type)
 */
public class AccountOptionView {

    private Integer accountId; // 실제 PK
    private String name;       // 계정명
    private String type;       // ASSET/LIABILITY/...

    private String label;      // 표시용(없으면 name/type로 생성)

    public AccountOptionView() {
        // reflection/setter 주입용
    }

    /** 가장 단순한 생성 */
    public AccountOptionView(Integer accountId, String name, String type) {
        this.accountId = accountId;
        this.name = name;
        this.type = type;
        this.label = buildLabel(name, type, null);
    }

    /** label까지 명시 */
    public AccountOptionView(Integer accountId, String name, String type, String label) {
        this.accountId = accountId;
        this.name = name;
        this.type = type;
        this.label = buildLabel(name, type, label);
    }

    /**
     * ✅ JournalService가 props=[accountId, name, id, label, type] 로 생성 시도하는 케이스 대응
     * - id는 accountId의 alias로 취급
     */
    public AccountOptionView(Integer accountId, String name, Integer id, String label, String type) {
        Integer resolvedId = (accountId != null ? accountId : id);
        this.accountId = resolvedId;
        this.name = name;
        this.type = type;
        this.label = buildLabel(name, type, label);
    }

    // ---------- getters/setters ----------
    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
        // label이 비어있으면 자동 생성
        if (isBlank(this.label)) {
            this.label = buildLabel(this.name, this.type, null);
        }
    }

    /**
     * id alias getter (템플릿/프로젝션 호환)
     */
    public Integer getId() {
        return accountId;
    }

    /**
     * id alias setter (템플릿/프로젝션 호환)
     */
    public void setId(Integer id) {
        this.accountId = id;
        if (isBlank(this.label)) {
            this.label = buildLabel(this.name, this.type, null);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (isBlank(this.label)) {
            this.label = buildLabel(this.name, this.type, null);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        if (isBlank(this.label)) {
            this.label = buildLabel(this.name, this.type, null);
        }
    }

    public String getLabel() {
        if (isBlank(this.label)) {
            this.label = buildLabel(this.name, this.type, null);
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    // ---------- helpers ----------
    private static String buildLabel(String name, String type, String explicitLabel) {
        if (!isBlank(explicitLabel)) {
            return explicitLabel;
        }
        if (isBlank(name)) {
            return null;
        }
        if (isBlank(type)) {
            return name;
        }
        return name + " [" + type + "]";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
