package com.smartbiz.erp.dto.client_contact;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientContactUpdateRequestDto {

    private String name;
    private String phone;
    private String position;
    private String email;

    private Boolean primary;
    private Boolean active;
}
