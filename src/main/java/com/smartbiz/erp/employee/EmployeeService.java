package com.smartbiz.erp.employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartbiz.erp.dept.Dept;
import com.smartbiz.erp.dept.DeptService;
import com.smartbiz.erp.employee.history.EmployeeHistoryService;
import com.smartbiz.erp.position.Position;
import com.smartbiz.erp.position.PositionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DeptService deptService;
    private final PositionService positionService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeHistoryService employeeHistoryService;


    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("해당 id가 존재하지 않습니다." + id));
    }

    public List<EmployeeListView> findAllWithSearch(String searchField, String keyword) {

        List<Employee> employees = findAll();

        Map<Long, String> deptNameMap = deptService.findAll().stream()
                .collect(Collectors.toMap(Dept::getDeptId, Dept::getName));

        Map<Long, String> rankNameMap = positionService.getRanks().stream()
                .collect(Collectors.toMap(Position::getPositionId, Position::getName));

        Map<Long, String> titleNameMap = positionService.getTitles().stream()
                .collect(Collectors.toMap(Position::getPositionId, Position::getName));

        List<EmployeeListView> list = employees.stream()
                .map(e -> new EmployeeListView(
                        e.getEmployeeId(),
                        e.getName(),
                        deptNameMap.getOrDefault(e.getDeptId(), "미지정"),
                        rankNameMap.getOrDefault(e.getRankId(), ""),
                        e.getTitleId() == null ? "없음" : titleNameMap.getOrDefault(e.getTitleId(), "없음"),
                        e.getHireDate(),
                        e.getStatus()
                ))
                .toList();

        if (keyword == null || keyword.isBlank()) {
            return list;
        }

        String kw = keyword.trim();

        return list.stream()
                .filter(v -> {
                    if (searchField == null || searchField.isBlank()) {
                        return containsAny(v, kw);
                    }
                    return switch (searchField) {
                        case "id" -> v.getEmployeeId() != null && String.valueOf(v.getEmployeeId()).contains(kw);
                        case "name" -> v.getName() != null && v.getName().contains(kw);
                        case "dept" -> v.getDeptName() != null && v.getDeptName().contains(kw);
                        case "rank" -> v.getRankName() != null && v.getRankName().contains(kw);
                        case "status" -> v.getStatus() != null && v.getStatus().contains(kw);
                        default -> containsAny(v, kw);
                    };
                })
                .toList();
    }

    private boolean containsAny(EmployeeListView v, String kw) {
        return (v.getEmployeeId() != null && String.valueOf(v.getEmployeeId()).contains(kw))
                || (v.getName() != null && v.getName().contains(kw))
                || (v.getDeptName() != null && v.getDeptName().contains(kw))
                || (v.getRankName() != null && v.getRankName().contains(kw))
                || (v.getStatus() != null && v.getStatus().contains(kw));
    }

	 // 직원 화면(폼) 저장 전용
	 // 폼에 없는 로그인 관련 컬럼이 null 로 덮이지 않도록 병합 저장한다
	 public Employee saveFromForm(Employee form, int sessionAuth) {

     boolean isNew = (form.getEmployeeId() == null);

     // 수정이고, 권한이 3(일반 사용자)이면 비밀번호만 변경 가능
     if (!isNew && sessionAuth >= 3) {

         Employee origin = employeeRepository.findById(form.getEmployeeId())
                 .orElseThrow(() -> new EmployeeNotFoundException("employee not found"));

         System.out.println("서비스:");
         System.out.println(form.getPassword().trim());

         if (isBlank(form.getPassword())) {
             throw new EmployeeValidationException("비밀번호를 입력하세요.");
         }

         origin.setPassword(passwordEncoder.encode(form.getPassword().trim()));
         return employeeRepository.save(origin);
     }

     // 여기부터는 신규 생성 or 권한 1~2(관리자/인사) 전체 수정
     validateForm(form);

     if (isNew) {

         if (isBlank(form.getLoginId())) {
             form.setLoginId(generateDefaultLoginId());
         }
         if (isBlank(form.getPassword())) {
             form.setPassword(passwordEncoder.encode("0000"));
         }
         if (form.getAuth() <= 0) {
             form.setAuth(3);
         }
         if (isBlank(form.getUseYn())) {
             form.setUseYn("Y");
         }
         if (form.getHireDate() == null) {
             form.setHireDate(LocalDate.now());
         }

         Employee saved = employeeRepository.save(form);

         employeeHistoryService.recordHire(
                 saved.getEmployeeId(),
                 saved.getDeptId(),
                 saved.getRankId(),
                 saved.getTitleId(),
                 saved.getHireDate()
         );

         return saved;
     }

     Employee origin = employeeRepository.findById(form.getEmployeeId())
             .orElseThrow(() -> new EmployeeNotFoundException("employee not found"));

     Long beforeDeptId = origin.getDeptId();
     Long beforeRankId = origin.getRankId();
     Long beforeTitleId = origin.getTitleId();

     origin.setName(form.getName());
     origin.setDeptId(form.getDeptId());
     origin.setRankId(form.getRankId());
     origin.setTitleId(form.getTitleId());
     origin.setHireDate(form.getHireDate());
     origin.setRetireDate(form.getRetireDate());
     origin.setStatus(form.getStatus());
     origin.setJumin(form.getJumin());
     origin.setPhone(form.getPhone());
     origin.setEmail(form.getEmail());
     origin.setAddress(form.getAddress());
     origin.setBaseSalary(form.getBaseSalary());
     origin.setBankName(form.getBankName());
     origin.setBankAccount(form.getBankAccount());
     origin.setLoginId(form.getLoginId().trim());
     origin.setAuth(form.getAuth());
     origin.setUseYn(form.getUseYn().trim());

     if (!isBlank(form.getPassword())) {
         origin.setPassword(passwordEncoder.encode(form.getPassword().trim()));
     }

     Employee saved = employeeRepository.save(origin);

     Long afterDeptId = saved.getDeptId();
     Long afterRankId = saved.getRankId();
     Long afterTitleId = saved.getTitleId();

     employeeHistoryService.recordChange(
             saved.getEmployeeId(),
             beforeDeptId, afterDeptId,
             beforeRankId, afterRankId,
             beforeTitleId, afterTitleId
     );

     return saved;
 }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    // 로그인에서 사용할 조회(사용중 계정만)
    public Employee findActiveByLoginId(String loginId) {
        return employeeRepository.findByLoginIdAndUseYn(loginId, "Y")
                .orElseThrow(() -> new EmployeeValidationException("사용 가능한 계정을 찾을 수 없습니다."));
    }

    private void validateForm(Employee e) {
        if (isBlank(e.getName())) {
            throw new EmployeeValidationException("이름은 필수입니다.");
        }
        if (e.getDeptId() == null) {
            throw new EmployeeValidationException("부서는 필수입니다.");
        }
        if (e.getRankId() == null) {
            throw new EmployeeValidationException("직급은 필수입니다.");
        }
        if (e.getHireDate() == null) {
            throw new EmployeeValidationException("입사일은 필수입니다.");
        }
        if (isBlank(e.getStatus())) {
            throw new EmployeeValidationException("상태는 필수입니다.");
        }
        if (isBlank(e.getJumin()) || e.getJumin().length() != 13) {
            throw new EmployeeValidationException("주민등록번호는 13자리로 입력하세요.");
        }
        if (e.getBaseSalary() == null) {
            throw new EmployeeValidationException("기본급은 필수입니다.");
        }
        if (!java.util.List.of("재직","휴직","퇴사").contains(e.getStatus())) {
            throw new EmployeeValidationException("상태 값이 올바르지 않습니다.");
        }
    }

    private String generateDefaultLoginId() {
        // 단순 기본값(중복 가능성이 매우 낮게)
        return "emp" + System.currentTimeMillis();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
