package com.smartbiz.erp.support;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @GetMapping("/support")
    public String supports(Model model,
				    		@RequestParam(value = "searchField", required = false, defaultValue = "title") String searchField,
				    		@RequestParam(value = "keyword", required = false) String keyword,
				    		@RequestParam(value = "yearMonth", required = false) String yearMonth,
				    		@RequestParam(value = "fromDate", required = false)
				    		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                            @RequestParam(value = "status", required = false) String status,
                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                            Pageable pageable) {

    	Page<Support> page = supportService.findSupports(
    	        searchField, keyword, yearMonth, fromDate, status, pageable
    	);

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("statuses", SupportStatus.values());

        return "support/support";
    }

    @GetMapping("/support/form")
    public String supportForm(Model model,
                              @RequestParam(value = "id", required = false) Long id) {

        Support post;
        if (id == null) {
            post = Support.builder()
                    .status(SupportStatus.DRAFT)
                    .build();
        } else {
            post = supportService.getDetail(id);
        }

        model.addAttribute("post", post);
        model.addAttribute("statuses", SupportStatus.values());
        
        return "support/support-form";
    }

    @PostMapping("/support/save")
    public String saveSupport(@ModelAttribute("post") Support form, HttpSession session) {

        Object empIdObj = session.getAttribute("employeeId");
        if (empIdObj == null) {
            return "redirect:/login";
        }

        Long employeeId = (empIdObj instanceof Long) ? (Long) empIdObj : Long.valueOf(empIdObj.toString());

        supportService.saveSupport(form, employeeId);
        return "redirect:/support";
    }

    @PostMapping("/support/delete")
    public String deleteSupport(@RequestParam("id") Long id) {
        supportService.deleteSupport(id);
        return "redirect:/support";
    }

    @GetMapping("/support-detail/{id}")
    public String supportDetail(@PathVariable("id") Long id, Model model) {

        Support post = supportService.getDetail(id);

        model.addAttribute("post", post);
        model.addAttribute("answers", supportService.getAnswers(id));
        return "support/support-detail";
    }
    
    @PostMapping("/support/answer/save")
    public String saveAnswer(@RequestParam("supportId") Long supportId,
                             @RequestParam("content") String content,
                             HttpSession session) {

        Object empIdObj = session.getAttribute("employeeId");
        if (empIdObj == null) {
            return "redirect:/login";
        }

        Long employeeId = (empIdObj instanceof Long)
                ? (Long) empIdObj
                : Long.valueOf(empIdObj.toString());

        supportService.addAnswer(supportId, employeeId, content);

        return "redirect:/support-detail/" + supportId;
    }
    
    @PostMapping("/support/answer/delete")
    public String deleteAnswer(@RequestParam("answerId") Long answerId,
                               @RequestParam("supportId") Long supportId) {

        supportService.deleteAnswer(answerId, supportId);

        return "redirect:/support-detail/" + supportId;
    }
}
