package com.smartbiz.erp.support;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportRepository extends JpaRepository<Support, Long> {

    @Query("""
        select s
        from Support s
        left join s.writer w
        where (:status is null or s.status = :status)
          and (:titleKeyword is null or :titleKeyword = '' or s.title like concat('%', :titleKeyword, '%'))
          and (:writerKeyword is null or :writerKeyword = '' or (w is not null and w.name like concat('%', :writerKeyword, '%')))
          and (:fromDateTime is null or s.createdAt >= :fromDateTime)
        order by s.createdAt desc, s.supportId desc
    """)
    Page<Support> searchSupports(
        @Param("status") SupportStatus status,
        @Param("titleKeyword") String titleKeyword,
        @Param("writerKeyword") String writerKeyword,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        Pageable pageable
    );

    @Query("""
        select s
        from Support s
        left join fetch s.writer w
        where s.supportId = :id
    """)
    Optional<Support> findDetail(@Param("id") Long id);
}
