package com.backup.server.service;

import com.backup.server.entity.BackupHistory;
import com.backup.server.enums.BackupStatus;
import com.backup.server.mapper.BackupHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BackupHistoryService extends ServiceImpl<BackupHistoryMapper, BackupHistory> {

    public Page<BackupHistory> pageList(long pageNum, long pageSize, Long taskId) {
        LambdaQueryWrapper<BackupHistory> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(BackupHistory::getTaskId, taskId);
        }
        wrapper.orderByDesc(BackupHistory::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public void cleanOldBackups(Long taskId, int retainDays) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(retainDays);
        List<BackupHistory> oldList = baseMapper.selectOldBackups(taskId, beforeTime);
        for (BackupHistory history : oldList) {
            try {
                if (history.getFilePath() != null) {
                    File file = new File(history.getFilePath());
                    if (file.exists()) {
                        Files.delete(file.toPath());
                        log.info("Deleted old backup file: {}", history.getFilePath());
                    }
                }
                removeById(history.getId());
            } catch (Exception e) {
                log.error("Delete old backup failed: {}", history.getFilePath(), e);
            }
        }
    }

    public long countRunning(Long taskId) {
        LambdaQueryWrapper<BackupHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BackupHistory::getTaskId, taskId)
                .eq(BackupHistory::getStatus, BackupStatus.RUNNING);
        return count(wrapper);
    }
}
