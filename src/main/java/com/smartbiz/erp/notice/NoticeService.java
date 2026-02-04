package com.smartbiz.erp.notice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final EmployeeRepository employeeRepository;

    public Page<Notice> findNoticesBySessionAuth(
            int sessionAuth,
            String searchField,
            String keyword,
            String yearMonth,
            LocalDate fromDate,
            String statusParam,
            Pageable pageable
    ) {

        NoticeStatus status = null;
        
        String sf = (searchField == null || searchField.isBlank()) ? "title" : searchField.trim();
        String kw = (keyword == null) ? "" : keyword.trim();

        String titleKeyword = null;
        String writerKeyword = null;

        if (!kw.isEmpty()) {
            if ("writer".equalsIgnoreCase(sf)) {
                writerKeyword = kw;
            } else {
                titleKeyword = kw;
            }
        }

        if (sessionAuth >= 3) {
            status = NoticeStatus.PUBLISHED;
        } else {
            if (statusParam != null && !statusParam.isBlank()) {
                try {
                    status = NoticeStatus.valueOf(statusParam.trim());
                } catch (Exception e) {
                    status = null;
                }
            }
        }

        LocalDateTime fromDateTime = null;

        String ym = (yearMonth == null) ? "" : yearMonth.trim();
        if (!ym.isEmpty()) {
            try {
                java.time.YearMonth parsed = java.time.YearMonth.parse(ym);
                fromDateTime = parsed.atDay(1).atStartOfDay();
            } catch (Exception e) {
                fromDateTime = null;
            }
        } else {
            fromDateTime = (fromDate == null) ? null : fromDate.atStartOfDay();
        }

        return noticeRepository.searchNotices(
                status,
                titleKeyword,
                writerKeyword,
                fromDateTime,
                pageable
        );
    }

    public List<Notice> findPinnedTop(int limit) {
        return noticeRepository
                .findByPinnedYnOrderByCreatedAtDesc("Y", PageRequest.of(0, limit))
                .getContent();
    }

    public Notice getDetail(Long id, int sessionAuth) {
        Notice notice = noticeRepository.findDetail(id)
                .orElseThrow(() -> new IllegalArgumentException("notice not found id=" + id));

        if (sessionAuth >= 3 && notice.getStatus() != NoticeStatus.PUBLISHED) {
            throw new IllegalArgumentException("no permission");
        }

        return notice;
    }

    public Notice saveNotice(Notice form, Long writerEmployeeId) {

        Employee writer = employeeRepository.findById(writerEmployeeId)
                .orElseThrow(() -> new IllegalArgumentException("writer employee not found id=" + writerEmployeeId));

        String pinned = (form.getPinnedYn() == null || form.getPinnedYn().isBlank()) ? "N" : form.getPinnedYn().trim();

        if (form.getNoticeId() != null) {
            Notice origin = noticeRepository.findById(form.getNoticeId())
                    .orElseThrow(() -> new IllegalArgumentException("notice not found id=" + form.getNoticeId()));

            origin.setTitle(form.getTitle() == null ? "" : form.getTitle().trim());
            origin.setContent(form.getContent() == null ? "" : form.getContent().trim());
            origin.setPinnedYn(pinned);

            if (form.getStatus() != null) {
                origin.setStatus(form.getStatus());
            }

            origin.setUpdatedAt(LocalDateTime.now());

            return noticeRepository.save(origin);
        }

        Notice notice = Notice.builder()
                .title(form.getTitle() == null ? "" : form.getTitle().trim())
                .content(form.getContent() == null ? "" : form.getContent().trim())
                .writer(writer)
                .status(form.getStatus() == null ? NoticeStatus.PUBLISHED : form.getStatus())
                .pinnedYn(pinned)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return noticeRepository.save(notice);
    }

    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("notice not found id=" + noticeId));

        noticeRepository.delete(notice);
    }
}
