package com.smartbiz.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_contact_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String position;

    @Column(length = 200)
    private String email;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary;

    @Column(name = "is_active", nullable = false)
    private Boolean active;
}