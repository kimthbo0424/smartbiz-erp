package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String code;    // 창고 코드

    @Column(length = 255)
    private String location;  // 창고 위치

    @Column(length = 100)
    private String manager;   // 담당자

    private Integer capacity; // 최대 수용량

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
