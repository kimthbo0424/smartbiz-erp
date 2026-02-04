package com.smartbiz.erp.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartbiz.erp.dto.client.ClientCreateRequestDto;
import com.smartbiz.erp.dto.client.ClientResponseDto;
import com.smartbiz.erp.dto.client.ClientUpdateRequestDto;
import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientRestController {

    private final ClientService clientService;

    // 목록 (페이징)
    @GetMapping
    public ApiResponse<Page<ClientResponseDto>> getClients(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                clientService.getClientPageResponse(page, size)
        );
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<ClientResponseDto> getClient(@PathVariable("id") Long id) {
        return ApiResponse.success(
                clientService.getResponseById(id)
        );
    }

    // 생성
    @PostMapping
    public ApiResponse<Void> create(
            @RequestBody @Valid ClientCreateRequestDto dto
    ) {
        clientService.create(dto);
        return ApiResponse.success(null);
    }

    // 수정
    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
    		@PathVariable("id") Long id,
            @RequestBody ClientUpdateRequestDto dto
    ) {
        clientService.update(id, dto);
        return ApiResponse.success(null);
    }

    // 비활성화
    @DeleteMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable("id") Long id) {
        clientService.deactivate(id);
        return ApiResponse.success(null);
    }
}

