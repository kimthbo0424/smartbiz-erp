package com.smartbiz.erp.repository;

import com.smartbiz.erp.entity.ClientContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientContactRepository extends JpaRepository<ClientContact, Long> {

    List<ClientContact> findByClient_Id(Long clientId);
}
