// src/main/java/com/smartbiz/erp/orders/repository/OrderItemRepository.java
package com.smartbiz.erp.orders.repository;

import com.smartbiz.erp.orders.domain.OrderItem;
import com.smartbiz.erp.orders.domain.OrderStatus;
import com.smartbiz.erp.orders.dto.report.ProductSalesRowView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select new com.smartbiz.erp.orders.dto.report.ProductSalesRowView(
            p.name,
            (coalesce(sum(oi.subtotal), 0) + coalesce(sum(oi.tax), 0))
        )
        from OrderItem oi
        join oi.order o
        join oi.product p
        where o.orderDate >= :from
          and o.orderDate < :toExclusive
          and o.status not in :excluded
        group by p.name
        order by (coalesce(sum(oi.subtotal), 0) + coalesce(sum(oi.tax), 0)) desc
    """)
    List<ProductSalesRowView> sumSalesGroupByProduct(@Param("from") LocalDateTime from,
                                                     @Param("toExclusive") LocalDateTime toExclusive,
                                                     @Param("excluded") List<OrderStatus> excluded,
                                                     Pageable pageable);

    // ✅ 1차: 재고 회전율 계산용(기간 내 총 판매수량)
    @Query("""
        select coalesce(sum(oi.quantity), 0)
        from OrderItem oi
        join oi.order o
        where o.orderDate >= :from
          and o.orderDate < :toExclusive
          and o.status not in :excluded
    """)
    Long sumQuantityByPeriod(@Param("from") LocalDateTime from,
                             @Param("toExclusive") LocalDateTime toExclusive,
                             @Param("excluded") List<OrderStatus> excluded);
}
