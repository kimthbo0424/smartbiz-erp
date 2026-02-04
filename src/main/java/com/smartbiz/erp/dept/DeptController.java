// DeptController.java
package com.smartbiz.erp.dept;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /** 부서 목록 */
    @GetMapping("/departments")
    public String list(Model model) {
        model.addAttribute("departments", deptService.findAllWithParentName());
        return "dept/departments";
    }

    /** 부서 등록/수정 폼 */
    @GetMapping("/departments/form")
    public String form(@RequestParam(value = "id", required = false) Long id,
                       Model model) {

        Dept dept = (id == null) ? new Dept() : deptService.findById(id);

        model.addAttribute("department", dept);
        model.addAttribute("parentOptions", deptService.findAll());

        return "dept/departments-form";
    }

    @PostMapping("/departments/save")
    public String save(@ModelAttribute Dept dept) {
        deptService.save(dept);
        return "redirect:/departments";
    }

    @PostMapping("/departments/delete")
    public String delete(@RequestParam("id") Long id) {
        deptService.delete(id);
        return "redirect:/departments";
    }
}
