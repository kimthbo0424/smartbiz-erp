package com.smartbiz.erp.accounting.repository;

import com.smartbiz.erp.accounting.domain.AccountingClose;
import com.smartbiz.erp.accounting.domain.AccountingCloseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountingCloseRepository extends JpaRepository<AccountingClose, Long> {

    List<AccountingClose> findTop50ByOrderByClosedAtDesc();

    Optional<AccountingClose> findById(Long id);

    @Query("""
        select c from AccountingClose c
        where c.status = 'CLOSED'
        order by c.closedTo desc, c.closedAt desc
    """)
    List<AccountingClose> findLatestClosed();

    default Optional<AccountingClose> findLatestClosedOne() {
        List<AccountingClose> list = findLatestClosed();
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.ofNullable(list.get(0));
    }

    boolean existsByPeriodKeyAndStatus(String periodKey, AccountingCloseStatus status);
}
