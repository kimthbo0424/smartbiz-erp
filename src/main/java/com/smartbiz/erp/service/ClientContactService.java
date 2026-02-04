package com.smartbiz.erp.service;

import com.smartbiz.erp.dto.client_contact.ClientContactCreateRequestDto;
import com.smartbiz.erp.dto.client_contact.ClientContactResponseDto;
import com.smartbiz.erp.dto.client_contact.ClientContactUpdateRequestDto;
import com.smartbiz.erp.entity.Client;
import com.smartbiz.erp.entity.ClientContact;
import com.smartbiz.erp.repository.ClientContactRepository;
import com.smartbiz.erp.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientContactService {

    private final ClientRepository clientRepository;
    private final ClientContactRepository clientContactRepository;

    private ClientContact getContactEntity(Long id) {
        return clientContactRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("ClientContact not found: " + id));
    }

    // 거래처별 담당자 목록 (Response DTO)
    @Transactional(readOnly = true)
    public List<ClientContactResponseDto> getContactsByClientResponse(Long clientId) {
        return clientContactRepository.findByClient_Id(clientId)
                .stream()
                .map(ClientContactResponseDto::new)
                .toList();
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public ClientContactResponseDto getContactResponse(Long id) {
        return new ClientContactResponseDto(getContactEntity(id));
    }

    // 생성
    public void createContact(ClientContactCreateRequestDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Client not found: " + dto.getClientId()));

        boolean makePrimary = Boolean.TRUE.equals(dto.getPrimary());

        // 대표 담당자 설정 시 기존 대표 해제
        if (makePrimary) {
            clientContactRepository.findByClient_Id(dto.getClientId())
                    .forEach(c -> c.setPrimary(false));
        }

        ClientContact contact = ClientContact.builder()
                .client(client)
                .name(dto.getName())
                .phone(dto.getPhone())
                .position(dto.getPosition())
                .email(dto.getEmail())
                .primary(makePrimary)
                .active(true)
                .build();

        clientContactRepository.save(contact);
    }

    // 수정
    public void updateContact(Long contactId, ClientContactUpdateRequestDto dto) {
        ClientContact contact = getContactEntity(contactId);

        if (dto.getName() != null) contact.setName(dto.getName());
        if (dto.getPhone() != null) contact.setPhone(dto.getPhone());
        if (dto.getPosition() != null) contact.setPosition(dto.getPosition());
        if (dto.getEmail() != null) contact.setEmail(dto.getEmail());
        if (dto.getActive() != null) contact.setActive(dto.getActive());

        // 대표 담당자 변경
        if (dto.getPrimary() != null) {
            if (Boolean.TRUE.equals(dto.getPrimary())) {
                clientContactRepository.findByClient_Id(contact.getClient().getId())
                        .stream()
                        .filter(c -> !c.getId().equals(contact.getId()))
                        .forEach(c -> c.setPrimary(false));
                contact.setPrimary(true);
            } else {
                contact.setPrimary(false);
            }
        }
    }
    
    // ClientContact는 보조 정보이므로 Hard Delete 허용
    // 삭제 (하드 삭제)
    public void deleteContact(Long contactId) {
        clientContactRepository.deleteById(contactId);
    }
}
