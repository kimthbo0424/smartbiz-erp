package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.client_contact.ClientContactCreateRequestDto;
import com.smartbiz.erp.dto.client_contact.ClientContactResponseDto;
import com.smartbiz.erp.dto.client_contact.ClientContactUpdateRequestDto;
import com.smartbiz.erp.service.ClientContactService;
import com.smartbiz.erp.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client/{clientId}/contacts")
public class ClientContactController {

    private final ClientContactService clientContactService;
    private final ClientService clientService;

    /**
     * (선택) 담당자 목록
     * ※ 실무에서는 client/detail 탭이 목록 역할
     */
    @GetMapping
    public String list(
            @PathVariable("clientId") Long clientId,
            Model model
    ) {
        List<ClientContactResponseDto> contacts =
                clientContactService.getContactsByClientResponse(clientId);

        model.addAttribute("client", clientService.getById(clientId));
        model.addAttribute("contacts", contacts);
        return "client/detail";
    }

    /**
     * 담당자 등록 폼
     * GET /client/{clientId}/contacts/create
     */
    @GetMapping("/create")
    public String createForm(
            @PathVariable("clientId") Long clientId,
            Model model
    ) {
        model.addAttribute("client", clientService.getById(clientId));
        model.addAttribute("contact",
                ClientContactCreateRequestDto.builder()
                        .clientId(clientId)
                        .build());

        return "client_contact/create";
    }

    /**
     * 담당자 등록 처리
     */
    @PostMapping("/create")
    public String create(
            @PathVariable("clientId") Long clientId,
            @ModelAttribute("contact") ClientContactCreateRequestDto dto
    ) {
        dto.setClientId(clientId);
        clientContactService.createContact(dto);
        return "redirect:/client/detail/" + clientId + "?tab=contacts";
    }

    /**
     * 담당자 수정 폼
     * GET /client/{clientId}/contacts/{contactId}/edit
     */
    @GetMapping("/{contactId}/edit")
    public String editForm(
            @PathVariable("clientId") Long clientId,
            @PathVariable("contactId") Long contactId,
            Model model
    ) {
        ClientContactResponseDto contact =
                clientContactService.getContactResponse(contactId);

        model.addAttribute("client", clientService.getById(clientId));
        model.addAttribute("contactId", contactId);
        model.addAttribute("contact",
                ClientContactUpdateRequestDto.builder()
                        .name(contact.getName())
                        .phone(contact.getPhone())
                        .email(contact.getEmail())
                        .position(contact.getPosition())
                        .primary(contact.getPrimary())
                        .active(contact.getActive())
                        .build());

        return "client_contact/edit";
    }

    /**
     * 담당자 수정 처리
     */
    @PostMapping("/{contactId}/edit")
    public String edit(
            @PathVariable("clientId") Long clientId,
            @PathVariable("contactId") Long contactId,
            @ModelAttribute("contact") ClientContactUpdateRequestDto dto
    ) {
        clientContactService.updateContact(contactId, dto);
        return "redirect:/client/detail/" + clientId + "?tab=contacts";
    }

    /**
     * 담당자 삭제(비활성화)
     */
    @PostMapping("/{contactId}/delete")
    public String delete(
            @PathVariable("clientId") Long clientId,
            @PathVariable("contactId") Long contactId
    ) {
        clientContactService.deleteContact(contactId);
        return "redirect:/client/detail/" + clientId + "?tab=contacts";
    }
}
