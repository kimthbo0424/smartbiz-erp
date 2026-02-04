package com.smartbiz.erp.notice;

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
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    public String notices(HttpSession session,
                          Model model,
                          @RequestParam(value = "searchField", required = false, defaultValue = "title") String searchField,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "yearMonth", required = false) String yearMonth,
                          @RequestParam(value = "fromDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                          @RequestParam(value = "status", required = false) String status,
                          @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                          Pageable pageable) {

        int sessionAuth = getSessionAuthOrDefault(session, 3);

        model.addAttribute("pinnedPosts", noticeService.findPinnedTop(50));

        Page<Notice> page = noticeService.findNoticesBySessionAuth(
                sessionAuth, searchField, keyword, yearMonth, fromDate, status, pageable
        );

        model.addAttribute("posts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("sessionAuth", sessionAuth);

        return "notice/notices";
    }

    @GetMapping("/notices/form")
    public String noticeForm(HttpSession session,
                             Model model,
                             @RequestParam(value = "id", required = false) Long id) {

        int sessionAuth = getSessionAuthOrDefault(session, 3);
        if (sessionAuth > 2) {
            return "redirect:/notices";
        }

        Notice notice;

        if (id == null) {
            notice = Notice.builder()
                    .status(NoticeStatus.PUBLISHED)
                    .pinnedYn("N")
                    .build();
        } else {
            notice = noticeService.getDetail(id, sessionAuth);
        }

        model.addAttribute("post", notice);
        return "notice/notice-form";
    }

    @PostMapping("/notices/save")
    public String saveNotice(@ModelAttribute("post") Notice form, HttpSession session) {

        int sessionAuth = getSessionAuthOrDefault(session, 3);
        if (sessionAuth > 2) {
            return "redirect:/notices";
        }

        Object empIdObj = session.getAttribute("employeeId");
        if (empIdObj == null) {
            return "redirect:/login";
        }

        Long employeeId = (empIdObj instanceof Long) ? (Long) empIdObj : Long.valueOf(empIdObj.toString());

        noticeService.saveNotice(form, employeeId);

        return "redirect:/notices";
    }

    @PostMapping("/notices/delete")
    public String deleteNotice(@RequestParam("id") Long id, HttpSession session) {

        int sessionAuth = getSessionAuthOrDefault(session, 3);
        if (sessionAuth > 2) {
            return "redirect:/notices";
        }

        noticeService.deleteNotice(id);
        return "redirect:/notices";
    }

    @GetMapping("/notice-detail/{id}")
    public String noticeDetail(@PathVariable("id") Long id,
                               HttpSession session,
                               Model model) {

        int sessionAuth = getSessionAuthOrDefault(session, 3);

        Notice post = noticeService.getDetail(id, sessionAuth);

        model.addAttribute("post", post);
        model.addAttribute("sessionAuth", sessionAuth);

        return "notice/notice-detail";
    }

    private int getSessionAuthOrDefault(HttpSession session, int defaultValue) {
        Object authObj = session.getAttribute("auth");
        if (authObj == null) return defaultValue;
        if (authObj instanceof Integer) return (Integer) authObj;

        try {
            return Integer.parseInt(authObj.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
