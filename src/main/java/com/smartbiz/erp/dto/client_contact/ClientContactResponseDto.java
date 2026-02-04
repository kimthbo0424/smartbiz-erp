package com.smartbiz.erp.dto.client_contact;

import com.smartbiz.erp.entity.ClientContact;
import lombok.Getter;

@Getter
public class ClientContactResponseDto {

    private final Long id;
    private final Long clientId;
    private final String name;
    private final String phone;
    private final String position;
    private final String email;
    private final Boolean primary;
    private final Boolean active;


    public ClientContactResponseDto(ClientContact c) {
        this.id = c.getId();
        this.clientId = c.getClient() != null ? c.getClient().getId() : null;
        this.name = c.getName();
        this.phone = c.getPhone();
        this.position = c.getPosition();
        this.email = c.getEmail();
        this.primary = c.getPrimary();
        this.active = c.getActive();
    }
}
