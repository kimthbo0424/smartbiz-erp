package com.smartbiz.erp.position;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long positionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PositionType type;   // RANK / TITLE

    @Column(nullable = false, length = 50)
    private String name;         // 명칭

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;   // 정렬 기준

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn;        // Y / N
}
