package com.smartbiz.erp.service;

import com.smartbiz.erp.config.RedisCacheConfig;
import com.smartbiz.erp.dto.product.ProductCreateRequestDto;
import com.smartbiz.erp.dto.product.ProductResponseDto;
import com.smartbiz.erp.dto.product.ProductUpdateRequestDto;
import com.smartbiz.erp.entity.Product;
import com.smartbiz.erp.entity.enums.ProductStatus;
import com.smartbiz.erp.repository.CategoryRepository;
import com.smartbiz.erp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /* ===============================
       조회 (READ)
       =============================== */

    // 목록 (페이징 + 정렬)
    public Page<ProductResponseDto> getProductPageResponse(
            int page, int size, String sort
    ) {
        Sort sortSpec = switch (sort) {
            case "recent_asc"  -> Sort.by("id").ascending();
            case "recent_desc" -> Sort.by("id").descending();
            case "name_asc"    -> Sort.by("name").ascending();
            case "name_desc"   -> Sort.by("name").descending();
            default            -> Sort.by("id").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sortSpec);

        return productRepository.findAll(pageable)
                .map(ProductResponseDto::new);
    }

    // 단건 조회
    public ProductResponseDto findByIdResponse(Long id) {
        return new ProductResponseDto(getById(id));
    }

    // 내부용 엔티티 조회
    private Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품이 존재하지 않습니다. id=" + id)
                );
    }
    
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
            .filter(Product::getIsActive)
            .map(ProductResponseDto::new)
            .toList();
    }


    /* ===============================
       생성 (CREATE)
       =============================== */

    @CacheEvict(
        value = RedisCacheConfig.CACHE_PRODUCT_LIST,
        allEntries = true
    )
    public void create(ProductCreateRequestDto dto) {

        ProductStatus status =
                dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE;

        Product p = Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .barcode(dto.getBarcode())
                .costPrice(dto.getCostPrice())
                .unitPrice(dto.getUnitPrice())
                .status(status)
                .isActive(status == ProductStatus.ACTIVE)
                .build();

        p.setCategory(
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"))
        );

        productRepository.save(p);
    }

    /* ===============================
       수정 (UPDATE)
       =============================== */

    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_PRODUCT, allEntries = true),
        @CacheEvict(value = RedisCacheConfig.CACHE_PRODUCT_LIST, allEntries = true)
    })
    public void updatePartial(Long id, ProductUpdateRequestDto dto) {

        Product p = getById(id);

        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getSku() != null) p.setSku(dto.getSku());
        if (dto.getBarcode() != null) p.setBarcode(dto.getBarcode());
        if (dto.getCostPrice() != null) p.setCostPrice(dto.getCostPrice());
        if (dto.getUnitPrice() != null) p.setUnitPrice(dto.getUnitPrice());

        if (dto.getStatus() != null) {
            p.setStatus(dto.getStatus());
        }

        if (dto.getIsActive() != null) {
            p.setIsActive(dto.getIsActive());
        }

        if (dto.getCategoryId() != null) {
            p.setCategory(
                    categoryRepository.findById(dto.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"))
            );
        }
    }

    /* ===============================
       비활성화 (SOFT DELETE)
       =============================== */

    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_PRODUCT, allEntries = true),
        @CacheEvict(value = RedisCacheConfig.CACHE_PRODUCT_LIST, allEntries = true)
    })
    public void deactivate(Long id) {
        Product p = getById(id);
        p.setIsActive(false);
        p.setStatus(ProductStatus.DISCONTINUED);
    }
}
