package com.smartbiz.erp.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollItemService {

    private final PayrollItemRepository payrollItemRepository;

    @Transactional(readOnly = true)
    public List<PayrollItem> findAll() {
        return payrollItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PayrollItem findById(Long id) {
        return payrollItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("payroll item not found"));
    }

    @Transactional(readOnly = true)
    public List<PayrollItem> findActiveItems() {
        return payrollItemRepository.findByUseYnOrderByItemTypeAscItemIdAsc("Y");
    }

    public void save(PayrollItem item) {
        payrollItemRepository.save(item);
    }

    public void delete(Long id) {
        payrollItemRepository.deleteById(id);
    }
}
