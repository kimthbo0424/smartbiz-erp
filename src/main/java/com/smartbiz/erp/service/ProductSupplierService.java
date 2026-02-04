package com.smartbiz.erp.service;

import com.smartbiz.erp.dto.product_supplier.ProductSupplierCreateDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierCreateRequestDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierResponseDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierUpdateDto;
import com.smartbiz.erp.dto.product_supplier.ProductSupplierUpdateRequestDto;
import com.smartbiz.erp.entity.Client;
import com.smartbiz.erp.entity.Product;
import com.smartbiz.erp.entity.ProductSupplier;
import com.smartbiz.erp.repository.ClientRepository;
import com.smartbiz.erp.repository.ProductRepository;
import com.smartbiz.erp.repository.ProductSupplierRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSupplierService {

    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final ProductSupplierRepository productSupplierRepository;

    /* =========================
       도메인 핵심 로직
       ========================= */

    public List<ProductSupplier> findByProduct(Long productId) {
        return productSupplierRepository.findByProduct_Id(productId);
    }

    public ProductSupplier findById(Long id) {
        return productSupplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductSupplier not found: " + id));
    }

    public void create(
            Long productId,
            Long clientId,
            String supplierSku,
            Integer leadTime,
            Boolean isPrimary
    ) {
        Product product = productRepository.findById(productId).orElseThrow();
        Client client = clientRepository.findById(clientId).orElseThrow();

        if (Boolean.TRUE.equals(isPrimary)) {
            clearPrimarySuppliers(productId);
        }

        ProductSupplier ps = ProductSupplier.builder()
                .product(product)
                .client(client)
                .supplierSku(supplierSku)
                .leadTime(leadTime)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .build();

        productSupplierRepository.save(ps);
    }

    public void update(
            Long id,
            String supplierSku,
            Integer leadTime,
            Boolean isPrimary
    ) {
        ProductSupplier ps = findById(id);

        if (Boolean.TRUE.equals(isPrimary)) {
            clearPrimarySuppliers(ps.getProduct().getId());
            ps.setIsPrimary(true);
        } else if (isPrimary != null) {
            ps.setIsPrimary(false);
        }

        if (supplierSku != null) ps.setSupplierSku(supplierSku);
        if (leadTime != null) ps.setLeadTime(leadTime);
    }

    public void delete(Long id) {
        productSupplierRepository.deleteById(id);
    }

    /* ========================= */

    private void clearPrimarySuppliers(Long productId) {
        productSupplierRepository.findByProduct_Id(productId)
                .forEach(ps -> ps.setIsPrimary(false));
    }
}
