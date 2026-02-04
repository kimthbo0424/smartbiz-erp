package com.smartbiz.erp.backup;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupRepository extends JpaRepository<Backup, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    List<Backup> findTop10ByOrderByBackupTimeDesc();

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<Backup> findTop1ByOrderByBackupTimeDesc();
}
