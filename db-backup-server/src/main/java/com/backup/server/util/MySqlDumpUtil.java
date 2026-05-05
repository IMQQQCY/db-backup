package com.backup.server.util;

import com.backup.server.entity.DataSource;
import com.backup.server.enums.BackupContent;
import com.backup.server.enums.BackupType;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class MySqlDumpUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static File executeDump(DataSource dataSource, BackupType backupType, String tableList,
                                    String outputDir, String taskName, BackupContent backupContent) throws Exception {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = taskName + "_" + timestamp + ".sql";
        String sqlFilePath = outputDir + File.separator + fileName;

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        java.util.List<String> command = buildCommand(dataSource, backupType, tableList, sqlFilePath, backupContent);
        log.info("Executing dump command: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(300, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("mysqldump 执行超时");
        }

        File sqlFile = new File(sqlFilePath);
        // 使用 --force 后可能 exit code 非0（部分视图/表报错），但只要文件正常生成就视为成功
        if (process.exitValue() != 0) {
            if (!sqlFile.exists() || sqlFile.length() == 0) {
                throw new RuntimeException("mysqldump 执行失败: " + output);
            }
            log.warn("mysqldump 完成但有警告(exit code={}): {}", process.exitValue(), output);
        }

        if (!sqlFile.exists() || sqlFile.length() == 0) {
            throw new RuntimeException("备份文件为空，请检查数据库连接和权限");
        }

        File gzipFile = compressGzip(sqlFile);
        Files.deleteIfExists(sqlFile.toPath());

        log.info("Backup completed: {}", gzipFile.getAbsolutePath());
        return gzipFile;
    }

    private static java.util.List<String> buildCommand(DataSource ds, BackupType backupType,
                                                        String tableList, String outputPath, BackupContent backupContent) {
        java.util.List<String> cmd = new java.util.ArrayList<>();
        cmd.add("mysqldump");
        cmd.add("-h" + ds.getHost());
        cmd.add("-P" + ds.getPort());
        cmd.add("-u" + ds.getUsername());
        cmd.add("-p" + ds.getPassword());
        cmd.add("--single-transaction");
        cmd.add("--routines");
        cmd.add("--triggers");
        cmd.add("--hex-blob");
        cmd.add("--force");
        cmd.add("--no-tablespaces");

        // 仅备份结构，不包含数据
        if (backupContent == BackupContent.STRUCTURE) {
            cmd.add("--no-data");
        }
        cmd.add("--databases");
        cmd.add(ds.getDatabaseName());

        if (backupType == BackupType.PARTIAL && tableList != null && !tableList.isEmpty()) {
            cmd.add("--tables");
            for (String table : tableList.split(",")) {
                String t = table.trim();
                if (!t.isEmpty()) {
                    cmd.add(t);
                }
            }
        }

        cmd.add("-r");
        cmd.add(outputPath);
        return cmd;
    }

    private static File compressGzip(File source) throws IOException {
        String gzipPath = source.getAbsolutePath() + ".gz";
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(gzipPath);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, len);
            }
        }
        return new File(gzipPath);
    }
}
