package com.smartbiz.erp.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartbiz.erp.dept.Dept;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public List<Position> getRanks() {
        return positionRepository.findByTypeOrderBySortOrderAsc(PositionType.RANK);
    }

    public List<Position> getTitles() {
        return positionRepository.findByTypeOrderBySortOrderAsc(PositionType.TITLE);
    }

    public Position getPosition(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("직급/직책을 찾을 수 없습니다. id=" + id));
    }

    public Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("직급/직책을 찾을 수 없습니다."));
    }

    public Position save(Position position) {
        return positionRepository.save(position);
    }

    public void delete(Long id) {
        positionRepository.deleteById(id);
    }
}
