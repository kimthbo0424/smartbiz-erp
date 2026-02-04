package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import com.smartbiz.erp.entity.enums.ClientType;

@Entity
@Table(name = "client")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    // CUSTOMER / SUPPLIER / BOTH
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ClientType type = ClientType.CUSTOMER;

    // 신용 한도
    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    // 결제 조건
    @Column(name = "payment_terms")
    private String paymentTerms;

    // 사업자 등록 번호
    @Column(name = "biz_no")
    private String bizNo;

    // 청구지
    @Column(name = "billing_addr")
    private String billingAddr;

    // 배송지
    @Column(name = "shipping_addr")
    private String shippingAddr;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
    
    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.type == null) {
            this.type = ClientType.CUSTOMER;
        }
    }
}
