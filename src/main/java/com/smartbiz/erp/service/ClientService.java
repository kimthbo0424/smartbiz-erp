package com.smartbiz.erp.service;

import com.smartbiz.erp.dto.client.ClientCreateDto;
import com.smartbiz.erp.dto.client.ClientCreateRequestDto;
import com.smartbiz.erp.dto.client.ClientResponseDto;
import com.smartbiz.erp.dto.client.ClientUpdateDto;
import com.smartbiz.erp.dto.client.ClientUpdateRequestDto;
import com.smartbiz.erp.entity.Client;
import com.smartbiz.erp.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    
    /* =================================================
    MVC 전용
    ================================================= */

    // 페이징
    public Page<Client> getClientPage(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()   // 수정
        );
        return clientRepository.findAll(pageable);
    }

    public Client getById(Long id) {
    	return clientRepository.findById(id)
    	        .orElseThrow(() -> new EntityNotFoundException("거래처 없음: " + id));
    }

    // 생성
    @Transactional
    public void create(ClientCreateDto dto) {
        Client c = new Client();
        c.setName(dto.getName());
        c.setType(dto.getType());
        c.setBizNo(dto.getBizNo());
        c.setBillingAddr(dto.getBillingAddr());
        c.setShippingAddr(dto.getShippingAddr());
        c.setCreditLimit(dto.getCreditLimit());
        c.setPaymentTerms(dto.getPaymentTerms());
        c.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        clientRepository.save(c);
    }

    // 수정
    @Transactional
    public void update(Long id, ClientUpdateDto dto) {
        Client c = getById(id);

        c.setName(dto.getName());
        c.setType(dto.getType());
        c.setBizNo(dto.getBizNo());
        c.setBillingAddr(dto.getBillingAddr());
        c.setShippingAddr(dto.getShippingAddr());
        c.setCreditLimit(dto.getCreditLimit());
        c.setPaymentTerms(dto.getPaymentTerms());

        c.setActive(dto.getIsActive() != null && dto.getIsActive());
    }

    /* =================================================
    REST 전용
    ================================================= */

 public Page<ClientResponseDto> getClientPageResponse(int page, int size) {
     Pageable pageable = PageRequest.of(
             page,
             size,
             Sort.by("id").descending()
     );

     return clientRepository.findAll(pageable)
             .map(this::toResponseDto);
 }

	 public ClientResponseDto getResponseById(Long id) {
	     return toResponseDto(getById(id));
	 }
	
	 @Transactional
	 public void create(ClientCreateRequestDto dto) {
	     Client c = Client.builder()
	             .name(dto.name())
	             .type(dto.type())
	             .bizNo(dto.bizNo())
	             .billingAddr(dto.billingAddr())
	             .shippingAddr(dto.shippingAddr())
	             .creditLimit(dto.creditLimit())
	             .paymentTerms(dto.paymentTerms())
	             .active(dto.isActive() != null ? dto.isActive() : true)
	             .build();
	
	     clientRepository.save(c);
	 }
	
	 @Transactional
	 public void update(Long id, ClientUpdateRequestDto dto) {
	     Client c = getById(id);
	
	     if (dto.name() != null) c.setName(dto.name());
	     if (dto.type() != null) c.setType(dto.type());
	     if (dto.bizNo() != null) c.setBizNo(dto.bizNo());
	     if (dto.billingAddr() != null) c.setBillingAddr(dto.billingAddr());
	     if (dto.shippingAddr() != null) c.setShippingAddr(dto.shippingAddr());
	     if (dto.creditLimit() != null) c.setCreditLimit(dto.creditLimit());
	     if (dto.paymentTerms() != null) c.setPaymentTerms(dto.paymentTerms());
	     if (dto.isActive() != null) c.setActive(dto.isActive());
	 }
	
	 @Transactional
	 public void deactivate(Long id) {
	     Client c = getById(id);
	     c.setActive(false);
	 }
	
	 /* =================================================
	    DTO 변환
	    ================================================= */
	
	 private ClientResponseDto toResponseDto(Client c) {
	     return new ClientResponseDto(
	             c.getId(),
	             c.getName(),
	             c.getType(),
	             c.getBizNo(),
	             c.getBillingAddr(),
	             c.getShippingAddr(),
	             c.getCreditLimit(),
	             c.getPaymentTerms(),
	             c.getActive()
	     );
	 }
}
