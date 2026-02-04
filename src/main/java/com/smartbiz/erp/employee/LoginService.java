package com.smartbiz.erp.employee;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee emp = employeeRepository.findByLoginIdAndUseYn(username, "Y")
                .orElseThrow(() -> new UsernameNotFoundException("not found"));

        String role = authToRole(emp.getAuth());

        return new org.springframework.security.core.userdetails.User(
                emp.getLoginId(),
                emp.getPassword(),
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(role))
        );
    }
    
    @Transactional(readOnly = true)
    public Long findEmployeeIdByLoginId(String loginId) {
        return employeeRepository.findByLoginIdAndUseYn(loginId, "Y")
                .map(Employee::getEmployeeId)
                .orElse(null);
    }

    @Transactional
    public void updateLastLoginAt(Long employeeId) {
        employeeRepository.findById(employeeId).ifPresent(e -> {
            e.touchLastLoginAt();
        });
    }

    private String authToRole(Integer auth) {
        if (auth == null) return "ROLE_USER";
        if (auth == 1) return "ROLE_ADMIN";
        if (auth == 2) return "ROLE_HR";
        return "ROLE_USER";
    }
}
