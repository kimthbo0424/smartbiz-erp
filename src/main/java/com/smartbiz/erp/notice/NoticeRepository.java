package com.smartbiz.erp.notice;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByPinnedYnOrderByCreatedAtDesc(String pinnedYn, Pageable pageable);

    @Query("""
        select n
        from Notice n
        left join n.writer w
        where n.pinnedYn <> 'Y'
          and (:status is null or n.status = :status)
          and (:titleKeyword is null or :titleKeyword = '' or n.title like concat('%', :titleKeyword, '%'))
          and (:writerKeyword is null or :writerKeyword = '' or (w is not null and w.name like concat('%', :writerKeyword, '%')))
          and (:fromDateTime is null or n.createdAt >= :fromDateTime)
        order by n.createdAt desc, n.noticeId desc
    """)
    Page<Notice> searchNotices(
        @Param("status") NoticeStatus status,
        @Param("titleKeyword") String titleKeyword,
        @Param("writerKeyword") String writerKeyword,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        Pageable pageable
    );

    @Query("""
        select n
        from Notice n
        left join fetch n.writer w
        where n.noticeId = :id
    """)
    Optional<Notice> findDetail(@Param("id") Long id);
}
