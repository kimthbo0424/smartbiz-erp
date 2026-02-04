package com.smartbiz.erp.controller;

import com.smartbiz.erp.dto.category.CategoryCreateDto;
import com.smartbiz.erp.dto.category.CategoryUpdateDto;
import com.smartbiz.erp.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list() {
        return "category/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryCreateDto());
        model.addAttribute("parents", categoryService.findAllForView());
        return "category/create";
    }
    
    @PostMapping("/create")
    public String createCategory(@ModelAttribute CategoryCreateDto dto) {
        categoryService.createForView(dto);
        return "redirect:/category/list";
    }


    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") Long id,
            Model model
    ) {
        model.addAttribute("category", categoryService.findEntityById(id));
        model.addAttribute("parents", categoryService.findAllForView());
        return "category/edit";
    }
    
    @PostMapping("/edit/{id}")
    public String updateCategory(
    		@PathVariable("id") Long id,
            @ModelAttribute CategoryUpdateDto dto
    ) {
    	System.out.println(">>> MVC update hit, id=" + id);
        categoryService.updateForView(id, dto);
        return "redirect:/category/list";
    }
}

