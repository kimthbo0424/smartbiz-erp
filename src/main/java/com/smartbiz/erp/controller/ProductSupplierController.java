package com.smartbiz.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product-supplier")
public class ProductSupplierController {
	
    // 공급사 목록 화면 (상품 기준)
    @GetMapping("/product/{productId}")
    public String list(@PathVariable("productId") Long productId) {
        return "product_supplier/list";
    }

    // 공급사 등록 화면
    @GetMapping("/product/{productId}/create")
    public String createForm(@PathVariable("productId") Long productId) {
        return "product_supplier/create";
    }

    // 공급사 수정 화면
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id) {
        return "product_supplier/edit";
    }
    
    @GetMapping("/{id}/detail")
    public String detailForm(
            @PathVariable("id") Long id,
            org.springframework.ui.Model model
    ) {
        model.addAttribute("supplierId", id);
        return "product_supplier/detail";
    }
}
