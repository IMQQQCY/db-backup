package com.backup.server.service;

import com.backup.server.config.BackupProperties;
import com.backup.server.entity.BackupHistory;
import com.backup.server.entity.BackupTask;
import com.backup.server.entity.DataSource;
import com.backup.server.entity.NfsConfig;
import com.backup.server.enums.BackupStatus;
import com.backup.server.enums.BackupType;
import com.backup.server.enums.StorageType;
import com.backup.server.util.MySqlDumpUtil;
import com.backup.server.util.NfsMountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Slf4j
@Service
public class BackupExecuteService {

    @Autowired
    private BackupTaskService backupTaskService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private BackupHistoryService backupHistoryService;
    @Autowired
    private NfsConfigService nfsConfigService;
    @Autowired
    private MailConfigService mailConfigService;
    @Autowired
    private BackupProperties backupProperties;

    public void executeBackup(Long taskId) {
        BackupTask task = backupTaskService.getById(taskId);
        if (task == null) {
            log.error("Backup task not found: {}", taskId);
            return;
        }

        DataSource dataSource = dataSourceService.getById(task.getDataSourceId());
        if (dataSource == null) {
            log.error("DataSource not found: {}", task.getDataSourceId());
            return;
        }

        long runningCount = backupHistoryService.countRunning(taskId);
        if (runningCount > 0) {
            log.warn("Task {} is already running, skip", taskId);
            return;
        }

        BackupHistory history = new BackupHistory();
        history.setTaskId(taskId);
        history.setTaskName(task.getName());
        history.setDataSourceId(task.getDataSourceId());
        history.setBackupType(task.getBackupType());
        history.setTableList(task.getTableList());
        history.setStatus(BackupStatus.RUNNING);
        history.setStartTime(LocalDateTime.now());
        history.setCreateTime(LocalDateTime.now());
        backupHistoryService.save(history);

        File backupFile = null;
        String finalPath = null;
        try {
            String tempDir = backupProperties.getTempDir();
            backupFile = MySqlDumpUtil.executeDump(dataSource, task.getBackupType(),
                    task.getTableList(), tempDir, task.getName(), task.getBackupContent());

            finalPath = moveToStorage(backupFile, task);

            history.setFilePath(finalPath);
            history.setFileSize(backupFile.length());
            history.setStatus(BackupStatus.SUCCESS);
            history.setEndTime(LocalDateTime.now());
            backupHistoryService.updateById(history);

            if (task.getRetainDays() != null && task.getRetainDays() > 0) {
                backupHistoryService.cleanOldBackups(taskId, task.getRetainDays());
            }

            mailConfigService.sendNotify(
                    "【备份成功】" + task.getName(),
                    String.format("任务: %s\n数据库: %s\n文件: %s\n大小: %s\n时间: %s",
                            task.getName(), dataSource.getDatabaseName(), finalPath,
                            formatSize(backupFile.length()), history.getEndTime())
            );

            log.info("Backup success: {}", finalPath);
        } catch (Exception e) {
            log.error("Backup failed: {}", task.getName(), e);
            history.setStatus(BackupStatus.FAILED);
            history.setErrorMsg(e.getMessage());
            history.setEndTime(LocalDateTime.now());
            backupHistoryService.updateById(history);

            mailConfigService.sendNotify(
                    "【备份失败】" + task.getName(),
                    String.format("任务: %s\n数据库: %s\n错误: %s\n时间: %s",
                            task.getName(), dataSource.getDatabaseName(), e.getMessage(), LocalDateTime.now())
            );
        } finally {
            if (backupFile != null && backupFile.exists() && !backupFile.getAbsolutePath().equals(finalPath)) {
                try {
                    Files.deleteIfExists(backupFile.toPath());
                } catch (Exception ignored) {
                }
            }
            if (task.getStorageType() == StorageType.NFS && task.getNfsConfigId() != null) {
                NfsConfig nfsConfig = nfsConfigService.getById(task.getNfsConfigId());
                if (nfsConfig != null) {
                    NfsMountUtil.unmount(nfsConfig.getLocalMountPoint());
                    nfsConfig.setMounted(false);
                    nfsConfigService.updateById(nfsConfig);
                }
            }
        }
    }

    private String moveToStorage(File backupFile, BackupTask task) throws Exception {
        if (task.getStorageType() == StorageType.LOCAL) {
            String targetDir = task.getStoragePath();
            File dir = new File(targetDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File targetFile = new File(targetDir + File.separator + backupFile.getName());
            Files.move(backupFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile.getAbsolutePath();
        } else if (task.getStorageType() == StorageType.NFS) {
            NfsConfig nfsConfig = nfsConfigService.getById(task.getNfsConfigId());
            if (nfsConfig == null) {
                throw new RuntimeException("NFS配置不存在");
            }
            boolean mounted = NfsMountUtil.mount(nfsConfig.getServer(), nfsConfig.getRemotePath(),
                    nfsConfig.getLocalMountPoint(), nfsConfig.getOptions());
            if (!mounted) {
                throw new RuntimeException("NFS挂载失败");
            }
            nfsConfig.setMounted(true);
            nfsConfigService.updateById(nfsConfig);

            String targetDir = nfsConfig.getLocalMountPoint() + File.separator +
                    (task.getStoragePath() != null ? task.getStoragePath() : "");
            File dir = new File(targetDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File targetFile = new File(targetDir + File.separator + backupFile.getName());
            Files.move(backupFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile.getAbsolutePath();
        }
        return backupFile.getAbsolutePath();
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024L * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
