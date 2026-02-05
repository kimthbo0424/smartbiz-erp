package com.smartbiz.erp.controller;

import com.smartbiz.erp.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final WarehouseService warehouseService;

    // 재고 현황
    @GetMapping("/status")
    public String statusPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/status";
    }

    // 재고 조정 리스트
    @GetMapping("/adjustment")
    public String adjustmentPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/adjustment";
    }

    // 재고 강제 조정 등록
    @GetMapping("/adjustment/form")
    public String adjustmentFormPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/adjustment-form";
    }

    // 입출고 내역
    @GetMapping("/inout")
    public String inoutPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/inout";
    }

    // 입출고 등록
    @GetMapping("/inout/form")
    public String inoutFormPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/inout-form";
    }

    // 재고 리포트
    @GetMapping("/report")
    public String reportPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/report";
    }

    // 재고 이동 이력
    @GetMapping("/move")
    public String movePage(Model model) {
        return "inventory/move";
    }

    // 재고 이동 등록
    @GetMapping("/move-form")
    public String moveFormPage(Model model) {
        model.addAttribute("warehouses", warehouseService.findAll());
        return "inventory/move-form";
    }
}