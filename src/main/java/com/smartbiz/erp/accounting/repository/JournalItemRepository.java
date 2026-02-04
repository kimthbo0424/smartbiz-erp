package com.smartbiz.erp.accounting.repository;

import com.smartbiz.erp.accounting.domain.JournalItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JournalItemRepository extends JpaRepository<JournalItem, Long> {

    @Query("""
        select coalesce(sum(i.debit - i.credit), 0)
        from JournalItem i
        join i.journal j
        where j.entryDate <= :to
          and i.account.type = 'ASSET'
          and j.status = 'POSTED'
    """)
    long sumAssets(@Param("to") LocalDateTime to);

    @Query("""
        select coalesce(sum(i.credit - i.debit), 0)
        from JournalItem i
        join i.journal j
        where j.entryDate <= :to
          and i.account.type = 'LIABILITY'
          and j.status = 'POSTED'
    """)
    long sumLiabilities(@Param("to") LocalDateTime to);

    @Query("""
        select coalesce(sum(i.credit - i.debit), 0)
        from JournalItem i
        join i.journal j
        where j.entryDate >= :from and j.entryDate < :to
          and i.account.type = 'REVENUE'
          and j.status = 'POSTED'
    """)
    long sumRevenue(@Param("from") LocalDateTime from,
                    @Param("to") LocalDateTime to);

    @Query("""
        select coalesce(sum(i.debit - i.credit), 0)
        from JournalItem i
        join i.journal j
        where j.entryDate >= :from and j.entryDate < :to
          and i.account.type = 'EXPENSE'
          and j.status = 'POSTED'
    """)
    long sumExpense(@Param("from") LocalDateTime from,
                    @Param("to") LocalDateTime to);

    /**
     * 입력 VAT(매입세액) 추정치:
     * - 계정과목 이름에 "부가세" 또는 "VAT" 가 포함된 라인의 debit 합을 사용합니다.
     * - 매출 VAT(예: "부가세예수금")은 보통 credit이므로 여기서는 자연스럽게 제외됩니다.
     */
    @Query("""
        select coalesce(sum(i.debit), 0)
        from JournalItem i
        join i.journal j
        where j.entryDate >= :from and j.entryDate < :to
          and j.status = 'POSTED'
          and i.debit > 0
          and (
            lower(i.account.name) like '%부가세%'
            or lower(i.account.name) like '%vat%'
          )
    """)
    long sumInputVat(@Param("from") LocalDateTime from,
                     @Param("to") LocalDateTime to);

    // -------------------------------------------------
    // ✅ 리포트(지출)용: 월별 지출 / Top 지출계정 / 최근 지출내역
    // -------------------------------------------------

    @Query("""
        select year(j.entryDate), month(j.entryDate),
               coalesce(sum(i.debit - i.credit), 0)
        from JournalItem i
        join i.journal j
        where j.status = 'POSTED'
          and j.entryDate >= :from and j.entryDate < :toExclusive
          and i.account.type = 'EXPENSE'
        group by year(j.entryDate), month(j.entryDate)
        order by year(j.entryDate), month(j.entryDate)
    """)
    List<Object[]> sumExpenseGroupByMonth(@Param("from") LocalDateTime from,
                                         @Param("toExclusive") LocalDateTime toExclusive);

    @Query("""
        select i.account.name, coalesce(sum(i.debit - i.credit), 0)
        from JournalItem i
        join i.journal j
        where j.status = 'POSTED'
          and j.entryDate >= :from and j.entryDate < :toExclusive
          and i.account.type = 'EXPENSE'
        group by i.account.name
        order by coalesce(sum(i.debit - i.credit), 0) desc
    """)
    List<Object[]> sumExpenseByAccount(@Param("from") LocalDateTime from,
                                      @Param("toExclusive") LocalDateTime toExclusive,
                                      Pageable pageable);

    @Query("""
        select j.id, j.entryDate, j.description, i.account.name, i.description,
               coalesce((i.debit - i.credit), 0)
        from JournalItem i
        join i.journal j
        where j.status = 'POSTED'
          and j.entryDate >= :from and j.entryDate < :toExclusive
          and i.account.type = 'EXPENSE'
        order by j.entryDate desc, j.id desc
    """)
    List<Object[]> findRecentExpenseLines(@Param("from") LocalDateTime from,
                                         @Param("toExclusive") LocalDateTime toExclusive,
                                         Pageable pageable);
}
