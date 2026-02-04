package com.smartbiz.erp.backup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartbiz.erp.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BackupService {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.mysqldump.path:}")
    private String mysqldumpPath;

    @Value("${backup.mysql.bin.dir:}")
    private String mysqlBinDir;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final EmployeeRepository employeeRepository;
    private final BackupRepository backupRepository;

    public File createFullBackupFile(Long createdByEmployeeId) {
        DbInfo info = parseMysqlJdbcUrl(jdbcUrl);

        try {
            File dir = new File("backups");
            if (!dir.exists()) dir.mkdirs();

            String filename = "backup_" + LocalDateTime.now().format(TS) + ".sql";
            File out = new File(dir, filename);

            List<String> cmd = new ArrayList<>();
            String dumpExe = resolveMysqldumpExecutable();
            
            File dumpFile = new File(dumpExe);
            if (dumpExe.contains("\\") || dumpExe.contains("/") || dumpExe.toLowerCase().endsWith(".exe")) {
                if (!dumpFile.exists()) {
                    throw new IllegalStateException("mysqldump not found " + dumpExe);
                }
            }
            
            cmd.add(dumpExe);
            cmd.add("-h");
            cmd.add(info.host);
            cmd.add("-P");
            cmd.add(String.valueOf(info.port));
            cmd.add("-u");
            cmd.add(dbUser);

            cmd.add("--routines");
            cmd.add("--triggers");
            cmd.add("--single-transaction");
            cmd.add("--set-gtid-purged=OFF");

            cmd.add(info.database);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Map<String, String> env = pb.environment();
            env.put("MYSQL_PWD", dbPassword);

            pb.redirectErrorStream(true);
            pb.redirectOutput(out);

            Process p = pb.start();
            int exit = p.waitFor();

            if (exit != 0) {
                throw new IllegalStateException("mysqldump failed exitCode=" + exit);
            }

            if (!out.exists() || Files.size(out.toPath()) == 0) {
                throw new IllegalStateException("backup file empty");
            }
            
            saveBackupLog(out, createdByEmployeeId);

            return out;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("backup failed", e);
        }
    }

    private static class DbInfo {
        String host;
        int port;
        String database;

        DbInfo(String host, int port, String database) {
            this.host = host;
            this.port = port;
            this.database = database;
        }
    }

    private DbInfo parseMysqlJdbcUrl(String url) {
        String raw = url;

        int idx = raw.indexOf("jdbc:mysql://");
        if (idx >= 0) raw = raw.substring("jdbc:mysql://".length());

        String hostPortAndDb = raw;
        int q = hostPortAndDb.indexOf("?");
        if (q >= 0) hostPortAndDb = hostPortAndDb.substring(0, q);

        String[] hpdb = hostPortAndDb.split("/", 2);
        String hostPort = hpdb[0];
        String db = hpdb.length > 1 ? hpdb[1] : "";

        String host = hostPort;
        int port = 3306;

        int colon = hostPort.indexOf(":");
        if (colon >= 0) {
            host = hostPort.substring(0, colon);
            port = Integer.parseInt(hostPort.substring(colon + 1));
        }

        if (db == null || db.isBlank()) {
            throw new IllegalArgumentException("db name not found in spring datasource url");
        }

        return new DbInfo(host, port, db);
    }
    
    private String resolveMysqldumpExecutable() {
        if (mysqldumpPath != null && !mysqldumpPath.isBlank()) {
            return mysqldumpPath.trim();
        }

        if (mysqlBinDir != null && !mysqlBinDir.isBlank()) {
            String dir = mysqlBinDir.trim();
            if (dir.endsWith("\\") || dir.endsWith("/")) {
                return dir + "mysqldump.exe";
            }
            return dir + "\\mysqldump.exe";
        }

        String os = System.getProperty("os.name");
        boolean isWindows = (os != null && os.toLowerCase().contains("win"));
        return isWindows ? "mysqldump.exe" : "mysqldump";
    }
    
    @Transactional
    public void saveBackupLog(File backupFile, Long createdByEmployeeId) {

        if (backupFile == null || !backupFile.exists()) {
            throw new IllegalArgumentException("backup file not found");
        }
        if (createdByEmployeeId == null) {
            throw new IllegalArgumentException("createdByEmployeeId is null");
        }
        
        com.smartbiz.erp.employee.Employee emp = employeeRepository.findById(createdByEmployeeId)
                .orElseThrow(() -> new IllegalArgumentException("employee not found id=" + createdByEmployeeId));

        Backup backup = Backup.builder()
                .fileName(backupFile.getName())
                .fileSize(backupFile.length())
                .backupTime(LocalDateTime.now())
                .createdBy(emp)
                .build();

        backupRepository.save(backup);
    }
    
    public Optional<Backup> getLatestBackup() {
        return backupRepository.findTop1ByOrderByBackupTimeDesc();
    }

    public List<Backup> getRecentBackups10() {
        return backupRepository.findTop10ByOrderByBackupTimeDesc();
    }
}
