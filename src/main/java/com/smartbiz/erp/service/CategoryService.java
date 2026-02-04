package com.smartbiz.erp.service;

import com.smartbiz.erp.config.RedisCacheConfig;
import com.smartbiz.erp.dto.category.CategoryCreateDto;
import com.smartbiz.erp.dto.category.CategoryUpdateDto;
import com.smartbiz.erp.dto.category.CategoryCreateRequestDto;
import com.smartbiz.erp.dto.category.CategoryUpdateRequestDto;
import com.smartbiz.erp.dto.category.CategoryResponseDto;
import com.smartbiz.erp.entity.Category;
import com.smartbiz.erp.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /* ===============================
       MVC 전용
       =============================== */

    public List<Category> findAllForView() {
        return categoryRepository.findAll();
    }

    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리 없음"));
    }

    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_CATEGORY_TREE, allEntries = true)
    public void createForView(CategoryCreateDto dto) {
        Category parent = null;
        int level = 1;

        if (dto.getParentId() != null) {
            parent = findEntityById(dto.getParentId());
            level = parent.getLevel() + 1;
        }

        Category c = Category.builder()
                .name(dto.getName())
                .parent(parent)
                .level(level)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        categoryRepository.save(c);
    }

    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_CATEGORY_TREE, allEntries = true)
    public void updateForView(Long id, CategoryUpdateDto dto) {
        Category c = findEntityById(id);

        if (dto.getName() != null) {
            c.setName(dto.getName());
        }

        if (dto.getParentId() != null) {
            Category parent = findEntityById(dto.getParentId());
            c.setParent(parent);
            c.setLevel(parent.getLevel() + 1);
        }

        if (dto.getParentId() == null && dto.getLevel() != null) {
            c.setLevel(dto.getLevel());
        }
        if (dto.getIsActive() != null) {
            c.setIsActive(dto.getIsActive());
        }
    }

    /* ===============================
       REST 전용
       =============================== */

    @Cacheable(
    	    value = RedisCacheConfig.CACHE_CATEGORY_TREE,
    	    key = "'all'"
    	)
    	public List<CategoryResponseDto> findAllFlat() {
    	    return categoryRepository.findAll().stream()
    	            .map(this::toDto)
    	            .toList();
    	}

    public CategoryResponseDto findByIdResponse(Long id) {
        return toDto(findEntityById(id));
    }

    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_CATEGORY_TREE, allEntries = true)
    public void createForApi(CategoryCreateRequestDto req) {
        Category parent = null;
        int level = 1;

        if (req.parentId() != null) {
            parent = findEntityById(req.parentId());
            level = parent.getLevel() + 1;
        }

        Category c = Category.builder()
                .name(req.name())
                .parent(parent)
                .level(level)
                .isActive(true)
                .build();

        categoryRepository.save(c);
    }

    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_CATEGORY_TREE, allEntries = true)
    public void updateForApi(Long id, CategoryUpdateRequestDto req) {
        Category c = findEntityById(id);

        if (req.name() != null) c.setName(req.name());

        if (req.parentId() != null) {
            Category parent = findEntityById(req.parentId());
            c.setParent(parent);
            c.setLevel(parent.getLevel() + 1);
        }

        if (req.isActive() != null) {
            c.setIsActive(req.isActive());
        }
    }

    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_CATEGORY_TREE, allEntries = true)
    public void deactivate(Long id) {
        findEntityById(id).setIsActive(false);
    }

    /* ===============================
       공통
       =============================== */

    private CategoryResponseDto toDto(Category c) {
        return new CategoryResponseDto(
                c.getId(),
                c.getName(),
                c.getLevel(),
                Boolean.TRUE.equals(c.getIsActive()),
                c.getParent() != null ? c.getParent().getId() : null
        );
    }
}
