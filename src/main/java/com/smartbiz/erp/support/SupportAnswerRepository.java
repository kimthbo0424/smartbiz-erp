package com.smartbiz.erp.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportAnswerRepository extends JpaRepository<SupportAnswer, Long> {

    List<SupportAnswer> findBySupportSupportIdOrderByCreatedAtAsc(Long supportId);
}
