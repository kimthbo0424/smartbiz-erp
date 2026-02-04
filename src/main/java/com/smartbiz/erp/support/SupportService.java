package com.smartbiz.erp.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRepository supportRepository;
    private final SupportAnswerRepository supportAnswerRepository;
    private final EmployeeRepository employeeRepository;

    public Page<Support> findSupports(
            String searchField,
            String keyword,
            String yearMonth,
            LocalDate fromDate,
            String statusParam,
            Pageable pageable
    ) {

        SupportStatus status = null;
        if (statusParam != null && !statusParam.isBlank()) {
            try {
                status = SupportStatus.valueOf(statusParam.trim());
            } catch (Exception e) {
                status = null;
            }
        }

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

        return supportRepository.searchSupports(
                status,
                titleKeyword,
                writerKeyword,
                fromDateTime,
                pageable
        );
    }

    public Support getDetail(Long id) {
        return supportRepository.findDetail(id)
                .orElseThrow(() -> new IllegalArgumentException("support not found id=" + id));
    }

    public Support saveSupport(Support form, Long writerEmployeeId) {

        Employee writer = employeeRepository.findById(writerEmployeeId)
                .orElseThrow(() -> new IllegalArgumentException("writer employee not found id=" + writerEmployeeId));

        if (form.getSupportId() != null) {
            Support origin = supportRepository.findById(form.getSupportId())
                    .orElseThrow(() -> new IllegalArgumentException("support not found id=" + form.getSupportId()));

            origin.setTitle(form.getTitle() == null ? "" : form.getTitle().trim());
            origin.setContent(form.getContent() == null ? "" : form.getContent().trim());

            if (form.getStatus() != null) {
                origin.setStatus(form.getStatus());
            }

            origin.setUpdatedAt(LocalDateTime.now());
            return supportRepository.save(origin);
        }

        Support support = Support.builder()
                .title(form.getTitle() == null ? "" : form.getTitle().trim())
                .content(form.getContent() == null ? "" : form.getContent().trim())
                .writer(writer)
                .status(SupportStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return supportRepository.save(support);
    }

    public void deleteSupport(Long supportId) {
        Support s = supportRepository.findById(supportId)
                .orElseThrow(() -> new IllegalArgumentException("support not found id=" + supportId));
        supportRepository.delete(s);
    }
    
    public List<SupportAnswer> getAnswers(Long supportId) {
        return supportAnswerRepository.findBySupportSupportIdOrderByCreatedAtAsc(supportId);
    }

    public void addAnswer(Long supportId, Long responderEmployeeId, String content) {

        if (supportId == null) {
            throw new IllegalArgumentException("supportId is null");
        }

        if (responderEmployeeId == null) {
            throw new IllegalArgumentException("responderEmployeeId is null");
        }

        String c = (content == null) ? "" : content.trim();
        if (c.isBlank()) {
            throw new IllegalArgumentException("content is blank");
        }

        Support support = supportRepository.findById(supportId)
                .orElseThrow(() -> new IllegalArgumentException("support not found id=" + supportId));
        
        support.setStatus(SupportStatus.ANSWERED);
        support.setUpdatedAt(LocalDateTime.now());
        supportRepository.save(support);

        Employee responder = employeeRepository.findById(responderEmployeeId)
                .orElseThrow(() -> new IllegalArgumentException("employee not found id=" + responderEmployeeId));

        SupportAnswer answer = SupportAnswer.builder()
                .support(support)
                .responder(responder)
                .content(c)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        supportAnswerRepository.save(answer);
    }

    public void deleteAnswer(Long answerId, Long supportId) {

        if (answerId == null) {
            throw new IllegalArgumentException("answerId is null");
        }

        SupportAnswer a = supportAnswerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("answer not found id=" + answerId));

        if (supportId != null) {
            Long sid = (a.getSupport() == null) ? null : a.getSupport().getSupportId();
            if (sid == null || sid.longValue() != supportId.longValue()) {
                throw new IllegalArgumentException("answer not match supportId");
            }
        }

        supportAnswerRepository.delete(a);
    }
}
