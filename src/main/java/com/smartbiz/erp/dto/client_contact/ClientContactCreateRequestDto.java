package com.smartbiz.erp.dto.client_contact;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientContactCreateRequestDto {

    private Long clientId;

    @NotBlank
    private String name;
    private String phone;
    private String position;
    private String email;

    private Boolean primary;
}
