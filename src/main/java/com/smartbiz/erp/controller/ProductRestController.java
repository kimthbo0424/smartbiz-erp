package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.dto.product.ProductCreateRequestDto;
import com.smartbiz.erp.dto.product.ProductResponseDto;
import com.smartbiz.erp.dto.product.ProductUpdateRequestDto;
import com.smartbiz.erp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;

    // 상품 목록 조회 (페이징 + 정렬)
    @GetMapping
    public ApiResponse<Page<ProductResponseDto>> getPage(
    		@RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "sort") String sort
    ) {
        return ApiResponse.success(
                productService.getProductPageResponse(page, size, sort)
        );
    }

    // 상품 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDto> getOne(
            @PathVariable("id") Long id
    ) {
        return ApiResponse.success(
                productService.findByIdResponse(id)
        );
    }

    // 상품 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(
            @Valid @RequestBody ProductCreateRequestDto dto
    ) {
        productService.create(dto);
        return ApiResponse.success(null, "상품이 생성되었습니다.");
    }

    // 상품 수정 (부분 수정)
    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequestDto dto
    ) {
        productService.updatePartial(id, dto);
        return ApiResponse.success(null, "상품이 수정되었습니다.");
    }

    // 상품 비활성화 (Soft Delete)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Long id
    ) {
        productService.deactivate(id);
    }
}
