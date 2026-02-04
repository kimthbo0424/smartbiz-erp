package com.smartbiz.erp.orders.repository;

import com.smartbiz.erp.orders.domain.Order;
import com.smartbiz.erp.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findTopByOrderNoStartingWithOrderByOrderNoDesc(String prefix);

    @EntityGraph(attributePaths = {"client", "items", "items.product"})
    Optional<Order> findWithAllById(Long id);

    // -----------------------------
    // 주문 목록 검색 (orderDate는 "해당 날짜 하루" 필터로 동작)
    // -----------------------------
    @EntityGraph(attributePaths = {"client"})
    @Query("""
        select o
        from Order o
        join o.client c
        where (:clientName is null or c.name like concat('%', :clientName, '%'))
          and (:managerName is null or o.managerName like concat('%', :managerName, '%'))
          and (:status is null or o.status = :status)
          and (:orderDateFrom is null or o.orderDate >= :orderDateFrom)
          and (:orderDateTo is null or o.orderDate < :orderDateTo)
        """)
    Page<Order> searchImpl(@Param("clientName") String clientName,
                           @Param("managerName") String managerName,
                           @Param("status") OrderStatus status,
                           @Param("orderDateFrom") LocalDateTime orderDateFrom,
                           @Param("orderDateTo") LocalDateTime orderDateTo,
                           Pageable pageable);

    default Page<Order> search(String clientName, String managerName, OrderStatus status, Pageable pageable) {
        return search(clientName, managerName, status, null, pageable);
    }

    default Page<Order> search(String clientName, String managerName, OrderStatus status, LocalDateTime orderDateFrom, Pageable pageable) {
        LocalDateTime to = (orderDateFrom == null ? null : orderDateFrom.plusDays(1));
        return searchImpl(clientName, managerName, status, orderDateFrom, to, pageable);
    }

    // -----------------------------
    // 회계(거래내역) 조회용
    // -----------------------------
    @EntityGraph(attributePaths = {"client"})
    @Query("""
        select o
        from Order o
        join o.client c
        where (:clientName is null or c.name like concat('%', :clientName, '%'))
          and (:from is null or o.orderDate >= :from)
          and (:toExclusive is null or o.orderDate < :toExclusive)
        """)
    Page<Order> searchAccounting(@Param("clientName") String clientName,
                                @Param("from") LocalDateTime from,
                                @Param("toExclusive") LocalDateTime toExclusive,
                                Pageable pageable);

    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status = :status
        """)
    BigDecimal sumTotalAmountBetween(@Param("from") LocalDateTime from,
                                    @Param("toExclusive") LocalDateTime toExclusive,
                                    @Param("status") OrderStatus status);

    @Query("""
        select coalesce(sum(o.taxAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status = :status
        """)
    BigDecimal sumTaxAmountBetween(@Param("from") LocalDateTime from,
                                  @Param("toExclusive") LocalDateTime toExclusive,
                                  @Param("status") OrderStatus status);

    long countByOrderDateBetweenAndStatusIn(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses);

    // -----------------------------
    // 손익/요약 계산용
    // -----------------------------
    @Query("""
        select coalesce(sum(o.subtotalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumSubtotalBetweenStatuses(@Param("from") LocalDateTime from,
                                         @Param("toExclusive") LocalDateTime toExclusive,
                                         @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select coalesce(sum(o.taxAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumTaxBetweenStatuses(@Param("from") LocalDateTime from,
                                    @Param("toExclusive") LocalDateTime toExclusive,
                                    @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumTotalBetweenStatuses(@Param("from") LocalDateTime from,
                                      @Param("toExclusive") LocalDateTime toExclusive,
                                      @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select coalesce(sum(o.profitAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumProfitBetweenStatuses(@Param("from") LocalDateTime from,
                                       @Param("toExclusive") LocalDateTime toExclusive,
                                       @Param("excluded") List<OrderStatus> excluded);

    // ✅ FIX: 2컬럼 -> 3컬럼(담당자, 매출합계(total), 이익합계(profit))로 반환
    @Query("""
        select o.managerName,
               coalesce(sum(o.totalAmount), 0),
               coalesce(sum(o.profitAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        group by o.managerName
        order by coalesce(sum(o.profitAmount), 0) desc
        """)
    List<Object[]> sumProfitByManager(@Param("from") LocalDateTime from,
                                     @Param("toExclusive") LocalDateTime toExclusive,
                                     @Param("excluded") List<OrderStatus> excluded);

    // -----------------------------
    // VAT (매출 기준)
    // -----------------------------
    @Query("""
        select coalesce(sum(o.subtotalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumSalesSubtotalForVat(@Param("from") LocalDateTime from,
                                     @Param("toExclusive") LocalDateTime toExclusive,
                                     @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select coalesce(sum(o.taxAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumSalesVatForVat(@Param("from") LocalDateTime from,
                                @Param("toExclusive") LocalDateTime toExclusive,
                                @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumSalesTotalForVat(@Param("from") LocalDateTime from,
                                  @Param("toExclusive") LocalDateTime toExclusive,
                                  @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select count(o)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    long countSalesForVat(@Param("from") LocalDateTime from,
                          @Param("toExclusive") LocalDateTime toExclusive,
                          @Param("excluded") List<OrderStatus> excluded);

    @EntityGraph(attributePaths = {"client"})
    @Query("""
        select o
        from Order o
        join o.client c
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        order by o.orderDate desc
        """)
    List<Order> findSalesOrdersForVat(@Param("from") LocalDateTime from,
                                      @Param("toExclusive") LocalDateTime toExclusive,
                                      @Param("excluded") List<OrderStatus> excluded,
                                      Pageable pageable);

    // -----------------------------
    // 리포트 집계용
    // -----------------------------
    @Query("""
        select coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    BigDecimal sumTotalByPeriod(@Param("from") LocalDateTime from,
                               @Param("toExclusive") LocalDateTime toExclusive,
                               @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select count(o)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    long countByPeriod(@Param("from") LocalDateTime from,
                       @Param("toExclusive") LocalDateTime toExclusive,
                       @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select year(o.orderDate), month(o.orderDate), coalesce(sum(o.totalAmount), 0)
        from Order o
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        group by year(o.orderDate), month(o.orderDate)
        order by year(o.orderDate), month(o.orderDate)
        """)
    List<Object[]> sumTotalGroupByMonth(@Param("from") LocalDateTime from,
                                       @Param("toExclusive") LocalDateTime toExclusive,
                                       @Param("excluded") List<OrderStatus> excluded);

    @Query("""
        select c.name, coalesce(sum(o.totalAmount), 0)
        from Order o
        join o.client c
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        group by c.name
        order by coalesce(sum(o.totalAmount), 0) desc
        """)
    List<Object[]> sumTotalGroupByClient(@Param("from") LocalDateTime from,
                                        @Param("toExclusive") LocalDateTime toExclusive,
                                        @Param("excluded") List<OrderStatus> excluded,
                                        Pageable pageable);

    @Query("""
        select count(distinct c.id)
        from Order o
        join o.client c
        where o.orderDate >= :from and o.orderDate < :toExclusive
          and o.status not in :excluded
        """)
    long countDistinctClientsByPeriod(@Param("from") LocalDateTime from,
                                     @Param("toExclusive") LocalDateTime toExclusive,
                                     @Param("excluded") List<OrderStatus> excluded);
}
