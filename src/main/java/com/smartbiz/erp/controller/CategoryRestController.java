package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.category.CategoryCreateRequestDto;
import com.smartbiz.erp.dto.category.CategoryUpdateRequestDto;
import com.smartbiz.erp.dto.category.CategoryResponseDto;
import com.smartbiz.erp.dto.common.ApiResponse;
import com.smartbiz.erp.service.CategoryService;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    // 전체 카테고리 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.findAllFlat())
        );
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategory(
    		@PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.findByIdResponse(id))
        );
    }

    // 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCategory(
            @Valid @RequestBody CategoryCreateRequestDto request
    ) {
        categoryService.createForApi(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }

    // 수정 (부분 수정)
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
    		@PathVariable("id") Long id,
            @Valid @RequestBody CategoryUpdateRequestDto request
    ) {
        categoryService.updateForApi(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 삭제 (Soft Delete)
    @DeleteMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable("id") Long id) {
        System.out.println(">>> DELETE deactivate hit: " + id);
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
