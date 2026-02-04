package com.smartbiz.erp.accounting.controller;

import com.smartbiz.erp.accounting.dto.JournalCreateForm;
import com.smartbiz.erp.accounting.dto.JournalDetailView;
import com.smartbiz.erp.accounting.service.JournalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounting/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    public String create(@ModelAttribute JournalCreateForm form, RedirectAttributes ra) {
        try {
            Long id = journalService.createManual(form);
            ra.addFlashAttribute("journalMessage", "전표가 등록되었습니다. (" + id + ")");
            return "redirect:/accounting?tab=journal";
        } catch (Exception e) {
            ra.addFlashAttribute("journalError", e.getMessage());
            return "redirect:/accounting?tab=journal";
        }
    }

    @PostMapping("/{id}/post")
    public String post(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            journalService.post(id);
            ra.addFlashAttribute("journalMessage", "전표가 확정(POSTED)되었습니다. (" + id + ")");
            return "redirect:/accounting?tab=journal";
        } catch (Exception e) {
            ra.addFlashAttribute("journalError", e.getMessage());
            return "redirect:/accounting?tab=journal";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        JournalDetailView view = journalService.getDetail(id);
        model.addAttribute("pageTitle", "SmartBiz ERP - 전표 상세");
        model.addAttribute("journal", view);
        return "accounting/journal_detail";
    }
}
