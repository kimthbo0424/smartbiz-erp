package com.smartbiz.erp.service;

import com.smartbiz.erp.config.RedisCacheConfig;
import com.smartbiz.erp.dto.warehouse.WarehouseCreateRequestDto;
import com.smartbiz.erp.dto.warehouse.WarehouseResponseDto;
import com.smartbiz.erp.dto.warehouse.WarehouseUpdateRequestDto;
import com.smartbiz.erp.entity.Warehouse;
import com.smartbiz.erp.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    /* =================================================
       MVC / 내부용
       ================================================= */

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 창고입니다."));
    }

    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public Warehouse update(Long id, Warehouse updated) {
        Warehouse origin = findById(id);

        origin.setName(updated.getName());
        origin.setCode(updated.getCode());
        origin.setLocation(updated.getLocation());
        origin.setManager(updated.getManager());
        origin.setCapacity(updated.getCapacity());
        origin.setIsActive(updated.getIsActive());

        return warehouseRepository.save(origin);
    }

    // 삭제 대신 비활성화
    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public void deactivate(Long id) {
        Warehouse wh = findById(id);
        wh.setIsActive(false);
        warehouseRepository.save(wh);
    }

    // 재활성화
    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public void activate(Long id) {
        Warehouse wh = findById(id);
        wh.setIsActive(true);
        warehouseRepository.save(wh);
    }

    /* =================================================
       REST API 전용
       ================================================= */

    // 전체 조회
    @Cacheable(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        key = "'all'"
    )
    public List<WarehouseResponseDto> findAllResponse() {
        return warehouseRepository.findAll()
                .stream()
                .map(WarehouseResponseDto::new)
                .toList();
    }

    // 단건 조회
    public WarehouseResponseDto findByIdResponse(Long id) {
        return new WarehouseResponseDto(findById(id));
    }

    // 생성 (REST)
    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public void create(WarehouseCreateRequestDto dto) {
        Warehouse w = Warehouse.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .location(dto.getLocation())
                .manager(dto.getManager())
                .capacity(dto.getCapacity())
                .isActive(true)
                .build();

        warehouseRepository.save(w);
    }

    // 수정 (REST)
    @CacheEvict(
        value = RedisCacheConfig.CACHE_WAREHOUSE_LIST,
        allEntries = true
    )
    public void update(Long id, WarehouseUpdateRequestDto dto) {
        Warehouse w = findById(id);

        if (dto.getName() != null) w.setName(dto.getName());
        if (dto.getCode() != null) w.setCode(dto.getCode());
        if (dto.getLocation() != null) w.setLocation(dto.getLocation());
        if (dto.getManager() != null) w.setManager(dto.getManager());
        if (dto.getCapacity() != null) w.setCapacity(dto.getCapacity());
        if (dto.getIsActive() != null) w.setIsActive(dto.getIsActive());

        warehouseRepository.save(w);
    }
}