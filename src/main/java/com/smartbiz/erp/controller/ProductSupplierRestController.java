package com.smartbiz.erp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;


import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierCreateRequestDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierResponseDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierUpdateRequestDto;
import com.smartbiz.erp.service.ProductSupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-suppliers")
public class ProductSupplierRestController {

    private final ProductSupplierService productSupplierService;

    @GetMapping("/by-product/{productId}")
    public ApiResponse<List<ProductSupplierResponseDto>> getByProduct(
    		@PathVariable("productId") Long productId
    ) {
        return ApiResponse.success(
            productSupplierService.findByProduct(productId)
                .stream()
                .map(ProductSupplierResponseDto::new)
                .toList()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductSupplierResponseDto> getOne(@PathVariable("id") Long id) {
        return ApiResponse.success(
            new ProductSupplierResponseDto(
                productSupplierService.findById(id)
            )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create(
            @Valid @RequestBody ProductSupplierCreateRequestDto dto
    ) {
        productSupplierService.create(
            dto.getProductId(),
            dto.getSupplierId(),
            dto.getSupplierSku(),
            dto.getLeadTime(),
            dto.getIsPrimary()
        );
        return ApiResponse.success(null, "공급처가 등록되었습니다.");
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> update(
    		@PathVariable("id") Long id,
            @RequestBody ProductSupplierUpdateRequestDto dto
    ) {
        productSupplierService.update(
            id,
            dto.getSupplierSku(),
            dto.getLeadTime(),
            dto.getIsPrimary()
        );
        return ApiResponse.success(null, "공급처 정보가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        productSupplierService.delete(id);
    }
}
