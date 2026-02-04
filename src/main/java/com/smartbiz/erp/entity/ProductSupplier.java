package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_supplier_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;   // 공급사(거래처)

    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;

    @Column(name = "lead_time")
    private Integer leadTime; // 리드타임(일)

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
