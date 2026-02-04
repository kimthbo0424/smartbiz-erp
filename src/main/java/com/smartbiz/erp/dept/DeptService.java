// DeptService.java
package com.smartbiz.erp.dept;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptRepository deptRepository;

    // 기존 findAll 대신, parentName 채워서 리턴하는 메서드
    public List<Dept> findAllWithParentName() {
        List<Dept> list = deptRepository.findAll();

        // dept_id -> name 맵
        Map<Long, String> nameMap = list.stream()
                .collect(Collectors.toMap(Dept::getDeptId, Dept::getName));

        // 각 부서에 parentName 세팅
        for (Dept d : list) {
            if (d.getParentId() != null) {
                d.setParentName(nameMap.get(d.getParentId()));
            }
        }
        return list;
    }

    public Dept findById(Long id) {
        return deptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("부서를 찾을 수 없습니다."));
    }

    public Dept save(Dept dept) {
        return deptRepository.save(dept);
    }

    public void delete(Long id) {
        deptRepository.deleteById(id);
    }

    // 상위부서 셀렉트용 (그냥 전체 돌려줘도 됨)
    public List<Dept> findAll() {
        return deptRepository.findAll();
    }
}
