package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.client.ClientCreateDto;
import com.smartbiz.erp.dto.client.ClientUpdateDto;
import com.smartbiz.erp.entity.Client;
import com.smartbiz.erp.service.ClientContactService;
import com.smartbiz.erp.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;
    private final ClientContactService clientContactService;

    // 거래처 목록
    @GetMapping("/list")
    public String list() {
        return "client/list";
    }

    // 거래처 등록 폼
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("client", new ClientCreateDto());
        return "client/create";
    }

    // 거래처 등록
    @PostMapping("/create")
    public String create(ClientCreateDto dto) {
        clientService.create(dto);
        return "redirect:/client/list";
    }

    // 거래처 상세 (탭 포함)
    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable("id") Long id,
            @RequestParam(name = "tab", defaultValue = "info") String tab,
            Model model
    ) {
        model.addAttribute("client", clientService.getById(id));
        model.addAttribute("contacts",
                clientContactService.getContactsByClientResponse(id));
        model.addAttribute("tab", tab);

        return "client/detail";
    }

    // 거래처 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("client", clientService.getById(id));
        return "client/edit";
    }

    // 거래처 수정
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable("id") Long id,
            @ModelAttribute ClientUpdateDto dto
    ) {
        clientService.update(id, dto);
        return "redirect:/client/detail/" + id;
    }
}
