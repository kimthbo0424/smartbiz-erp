package com.smartbiz.erp.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /** 직급/직책 목록 */
    @GetMapping("/positions")
    public String list(Model model) {
        model.addAttribute("ranks", positionService.getRanks());
        model.addAttribute("titles", positionService.getTitles());
        return "position/positions";
    }

    /** 직급 폼 (신규/수정) */
    @GetMapping("/ranks-form")
    public String rankForm(@RequestParam(value = "id", required = false) Long id,
                           Model model) {

        Position position;
        if (id != null) {
            position = positionService.getPosition(id);
        } else {
            position = Position.builder()
                    .type(PositionType.RANK)
                    .useYn("Y")
                    .sortOrder(1)
                    .build();
        }

        position.setType(PositionType.RANK); // 수정 시에도 확실히 고정
        model.addAttribute("position", position);
        return "position/ranks-form";
    }

    @PostMapping("/ranks-form")
    public String saveRank(@ModelAttribute("position") Position position) {
        position.setType(PositionType.RANK);
        if (position.getUseYn() == null) {
            position.setUseYn("Y");
        }
        positionService.save(position);
        return "redirect:/positions";
    }

    /** 직책 폼 (신규/수정) */
    @GetMapping("/titles-form")
    public String titleForm(@RequestParam(value = "id", required = false) Long id,
                            Model model) {

        Position position;
        if (id != null) {
            position = positionService.getPosition(id);
        } else {
            position = Position.builder()
                    .type(PositionType.TITLE)
                    .useYn("Y")
                    .sortOrder(1)
                    .build();
        }

        position.setType(PositionType.TITLE);
        model.addAttribute("position", position);
        return "position/titles-form";
    }

    @PostMapping("/titles-form")
    public String saveTitle(@ModelAttribute("position") Position position) {
        position.setType(PositionType.TITLE);
        if (position.getUseYn() == null) {
            position.setUseYn("Y");
        }
        positionService.save(position);
        return "redirect:/positions";
    }

    /** 삭제 (직급/직책 공통) */
    @PostMapping("/positions/delete")
    public String delete(@RequestParam(value = "id") Long id) {
        positionService.delete(id);
        return "redirect:/positions";
    }
}
