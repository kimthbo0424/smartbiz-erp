package com.smartbiz.erp.employee;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartbiz.erp.attendance.AttendanceService;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        HttpSession session = request.getSession();
        
        String loginId = authentication.getName();
        
        employeeRepository.findByLoginIdAndUseYn(loginId, "Y").ifPresent(emp -> {
            Long employeeId = emp.getEmployeeId();
            attendanceService.createTodayCheckInIfAbsent(employeeId);
            emp.setLastLoginAt(LocalDateTime.now());
            employeeRepository.save(emp);

            session.setAttribute("employeeId", employeeId);
            session.setAttribute("employeeName", emp.getName());
            session.setAttribute("auth", emp.getAuth());
        });

        response.sendRedirect("/dashboard");
    }
}
