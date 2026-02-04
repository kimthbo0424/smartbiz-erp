package com.smartbiz.erp.accounting.repository;

import com.smartbiz.erp.accounting.domain.JournalEntry;
import com.smartbiz.erp.accounting.domain.JournalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("""
        select je from JournalEntry je
        where (:status is null or je.status = :status)
          and (:from is null or je.entryDate >= :from)
          and (:to is null or je.entryDate < :to)
        order by je.entryDate desc
    """)
    Page<JournalEntry> search(
            @Param("status") JournalStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"items", "items.account"})
    @Query("select je from JournalEntry je where je.id in :ids")
    List<JournalEntry> findWithItemsByIdIn(@Param("ids") List<Long> ids);

    // ✅ 추가: 상세 조회용
    @EntityGraph(attributePaths = {"items", "items.account"})
    Optional<JournalEntry> findWithItemsById(Long id);
}
