package com.smartbiz.erp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse")
public class WarehouseController {

    // 창고 목록 화면
	@GetMapping("/list")
	public String list() {
	    return "warehouse/list";
	}

    // 창고 등록 화면
    @GetMapping("/create")
    public String createForm() {
        return "warehouse/create";
    }

    // 창고 상세 화면
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id) {
        return "warehouse/detail";
    }

    // 창고 수정 화면
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id) {
        return "warehouse/edit";
    }
}
