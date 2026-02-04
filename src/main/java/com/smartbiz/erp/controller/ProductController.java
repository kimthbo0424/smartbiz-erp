package com.smartbiz.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    //* 상품 목록 화면
    @GetMapping("/list")
    public String list() {
        return "product/list";
    }

    // 상품 등록 화면
    @GetMapping("/create")
    public String createForm() {
        return "product/create";
    }

    // 상품 상세 화면
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id) {
        return "product/detail";
    }

    // 상품 수정 화면
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id) {
        return "product/edit";
    }
}
