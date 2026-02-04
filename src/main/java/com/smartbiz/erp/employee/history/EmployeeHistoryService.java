package com.smartbiz.erp.employee.history;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.smartbiz.erp.dept.Dept;
import com.smartbiz.erp.dept.DeptService;
import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeRepository;
import com.smartbiz.erp.position.Position;
import com.smartbiz.erp.position.PositionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeHistoryService {

    private final EmployeeHistoryRepository employeeHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final DeptService deptService;
    private final PositionService positionService;


    // 화면용 리스트 가져오기 + 검색
    public List<EmployeeHistoryView> getHistoryList(String searchField, String keyword) {

        // 변경일 최신순 정렬로 전체 이력 조회
        List<EmployeeHistory> histories = employeeHistoryRepository.findAllOrderByChangeDateDesc();

        // 직원 이름 맵
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, String> empNameMap = employees.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Employee::getName));

        // 부서 이름 맵
        List<Dept> depts = deptService.findAll();
        Map<Long, String> deptNameMap = depts.stream()
                .collect(Collectors.toMap(Dept::getDeptId, Dept::getName));

        // 직급 / 직책 이름 맵
        List<Position> ranks = positionService.getRanks();
        List<Position> titles = positionService.getTitles();

        Map<Long, String> positionNameMap = new HashMap<>();
        for (Position p : ranks) {
            positionNameMap.put(p.getPositionId(), p.getName());
        }
        for (Position p : titles) {
            positionNameMap.put(p.getPositionId(), p.getName());
        }

        // 먼저 전체 DTO 리스트 생성
        List<EmployeeHistoryView> list = histories.stream()
                .map(h -> new EmployeeHistoryView(
                        h.getEmployeeId(),
                        empNameMap.getOrDefault(h.getEmployeeId(), "미지정"),
                        h.getChangeDate(),
                        h.getBeforeDeptId() != null ? deptNameMap.get(h.getBeforeDeptId()) : null,
                        h.getAfterDeptId() != null ? deptNameMap.get(h.getAfterDeptId()) : null,
                        h.getBeforePositionId() != null ? positionNameMap.get(h.getBeforePositionId()) : null,
                        h.getAfterPositionId() != null ? positionNameMap.get(h.getAfterPositionId()) : null,
                        h.getChangeType()
                ))
                .toList();

        // 검색어 없으면 그대로 반환
        if (keyword == null || keyword.isBlank()) {
            return list;
        }

        String kw = keyword; // lambda 에서 사용

        // 검색 대상에 따라 필터링
        return list.stream()
                .filter(v -> {
                    if ("empId".equals(searchField)) {
                        return String.valueOf(v.getEmpId()).contains(kw);
                    } else if ("empName".equals(searchField)) {
                        return v.getEmpName() != null && v.getEmpName().contains(kw);
                    } else if ("changeType".equals(searchField)) {
                        return v.getChangeType() != null && v.getChangeType().contains(kw);
                    } else {
                        // 혹시 searchField 가 이상한 값이면 세 필드 전체에서 검색
                        return String.valueOf(v.getEmpId()).contains(kw)
                                || (v.getEmpName() != null && v.getEmpName().contains(kw))
                                || (v.getChangeType() != null && v.getChangeType().contains(kw));
                    }
                })
                .toList();
    }

    // 신규 입사 기록
    public void recordHire(Long employeeId, Long deptId, Long rankId, Long titleId, LocalDate hireDate) {

        EmployeeHistory h = EmployeeHistory.builder()
                .employeeId(employeeId)
                .changeType("입사")
                .changeDate(hireDate != null ? hireDate : LocalDate.now())
                .beforeDeptId(null)
                .afterDeptId(deptId)
                .beforePositionId(null)
                .afterPositionId(rankId)   // 기본은 rank 를 넣음
                .note(titleId != null ? ("title:" + titleId) : null)
                .build();

        employeeHistoryRepository.save(h);
    }
    
    public void recordChange(
            Long employeeId,
            Long beforeDeptId, Long afterDeptId,
            Long beforeRankId, Long afterRankId,
            Long beforeTitleId, Long afterTitleId
    ) {
        boolean deptChanged = !equalsLong(beforeDeptId, afterDeptId);
        boolean rankChanged = !equalsLong(beforeRankId, afterRankId);
        boolean titleChanged = !equalsLong(beforeTitleId, afterTitleId);

        // 아무것도 안 바뀌면 기록 안 함
        if (!deptChanged && !rankChanged && !titleChanged) {
            return;
        }

        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();

     // 1 부서 이동
        if (deptChanged) {
        	String note = deptName(beforeDeptId) + " → " + deptName(afterDeptId);

        	EmployeeHistory h = EmployeeHistory.builder()
        	        .employeeId(employeeId)
        	        .changeType("부서 이동")
        	        .changeDate(today)
        	        .beforeDeptId(beforeDeptId)
        	        .afterDeptId(afterDeptId)
        	        .beforePositionId(null)
        	        .afterPositionId(null)
        	        .note(note)
        	        .build();
        	employeeHistoryRepository.save(h);
        }

        // 2 직급 변경
        if (rankChanged) {
        	String note = positionName(beforeRankId) + " → " + positionName(afterRankId);

        	EmployeeHistory h = EmployeeHistory.builder()
        	        .employeeId(employeeId)
        	        .changeType("직급 변경")
        	        .changeDate(today)
        	        .beforeDeptId(null)
        	        .afterDeptId(null)
        	        .beforePositionId(beforeRankId)
        	        .afterPositionId(afterRankId)
        	        .note(note)
        	        .build();
        	employeeHistoryRepository.save(h);
        }

        // 3 직책 변경
        if (titleChanged) {
        	String note = positionName(beforeTitleId) + " → " + positionName(afterTitleId);

        	EmployeeHistory h = EmployeeHistory.builder()
        	        .employeeId(employeeId)
        	        .changeType("직책 변경")
        	        .changeDate(today)
        	        .beforeDeptId(null)
        	        .afterDeptId(null)
        	        .beforePositionId(beforeTitleId)
        	        .afterPositionId(afterTitleId)
        	        .note(note)
        	        .build();
        	employeeHistoryRepository.save(h);
        }
    }

    private boolean equalsLong(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.longValue() == b.longValue();
    }
    
    private String deptName(Long deptId) {
        if (deptId == null) return "없음";
        return deptService.findById(deptId)
                .getName();
    }
    
    private String positionName(Long positionId) {
        if (positionId == null) return "없음";
        return positionService.findById(positionId)
                .getName();
    }

}
