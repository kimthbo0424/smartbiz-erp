package com.smartbiz.erp.backup;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    // 백업 화면 진입용
    @GetMapping("/backup/backup")
    public String page(Model model) {
        model.addAttribute("latestBackup", backupService.getLatestBackup().orElse(null));
        model.addAttribute("backups", backupService.getRecentBackups10());
        
        return "backup/backup";
    }

    @PostMapping("/backup/full")
    @ResponseBody
    public ResponseEntity<Resource> fullBackupDownload(HttpSession session) {

        Object empIdObj = session.getAttribute("employeeId");
        if (empIdObj == null) {
            // 로그인 세션 없으면 백업 불가
            return ResponseEntity.status(401).build();
        }

        Long empId = (empIdObj instanceof Long) ? (Long) empIdObj : Long.valueOf(empIdObj.toString());

        File file = backupService.createFullBackupFile(empId);
        Resource resource = new FileSystemResource(file);

        String encodedName = org.springframework.web.util.UriUtils.encode(
                file.getName(), StandardCharsets.UTF_8
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")

                // 다운로드 완료 감지용 쿠키
                .header(HttpHeaders.SET_COOKIE, "fileDownload=1; Path=/; SameSite=Lax")

                // 캐시 방지(권장)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .header("Pragma", "no-cache")
                .body(resource);
    }
}
