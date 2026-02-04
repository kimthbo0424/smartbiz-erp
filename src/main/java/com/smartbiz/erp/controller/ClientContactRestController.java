package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.client_contact.ClientContactCreateRequestDto;
import com.smartbiz.erp.dto.client_contact.ClientContactResponseDto;
import com.smartbiz.erp.dto.client_contact.ClientContactUpdateRequestDto;
import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.service.ClientContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/client-contacts")
public class ClientContactRestController {

    private final ClientContactService clientContactService;

    /**
     * 거래처 담당자 목록 조회
     * GET /api/client-contacts?clientId=1
     */
    @GetMapping
    public ApiResponse<List<ClientContactResponseDto>> getByClient(
            @RequestParam("clientId") Long clientId
    ) {
        return ApiResponse.success(
                clientContactService.getContactsByClientResponse(clientId)
        );
    }

    /**
     * 담당자 단건 조회
     * GET /api/client-contacts/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<ClientContactResponseDto> getOne(
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(
                clientContactService.getContactResponse(id)
        );
    }

    /**
     * 담당자 생성
     * POST /api/client-contacts
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(
            @Valid @RequestBody ClientContactCreateRequestDto dto
    ) {
        clientContactService.createContact(dto);
        return ApiResponse.success(null, "담당자가 등록되었습니다.");
    }

    /**
     * 담당자 수정
     * PATCH /api/client-contacts/{id}
     */
    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable("id") Long id,
            @RequestBody ClientContactUpdateRequestDto dto
    ) {
        clientContactService.updateContact(id, dto);
        return ApiResponse.success(null, "담당자가 수정되었습니다.");
    }

    /**
     * 담당자 삭제 (Soft Delete)
     * DELETE /api/client-contacts/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Long id
    ) {
        clientContactService.deleteContact(id);
    }
}
