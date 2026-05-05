package com.backup.server.service;

import com.backup.server.entity.BackupTask;
import com.backup.server.mapper.BackupTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class BackupTaskService extends ServiceImpl<BackupTaskMapper, BackupTask> {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            List<BackupTask> enabledTasks = list(new LambdaQueryWrapper<BackupTask>()
                    .eq(BackupTask::getEnabled, true));
            for (BackupTask task : enabledTasks) {
                scheduleJob(task);
            }
            log.info("Initialized {} scheduled backup tasks", enabledTasks.size());
        } catch (Exception e) {
            log.error("Init scheduled tasks failed", e);
        }
    }

    public Page<BackupTask> pageList(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<BackupTask> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(BackupTask::getName, keyword);
        }
        wrapper.orderByDesc(BackupTask::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public boolean addTask(BackupTask task) {
        save(task);
        if (Boolean.TRUE.equals(task.getEnabled()) && task.getCronExpression() != null) {
            try {
                scheduleJob(task);
            } catch (Exception e) {
                log.error("Schedule job failed", e);
                throw new RuntimeException("定时任务创建失败: " + e.getMessage());
            }
        }
        return true;
    }

    public boolean updateTask(BackupTask task) {
        BackupTask old = getById(task.getId());
        if (old == null) {
            throw new RuntimeException("任务不存在");
        }
        updateById(task);
        try {
            removeJob(old.getId());
            if (Boolean.TRUE.equals(task.getEnabled()) && task.getCronExpression() != null) {
                scheduleJob(task);
            }
        } catch (Exception e) {
            log.error("Reschedule job failed", e);
        }
        return true;
    }

    public boolean deleteTask(Long id) {
        try {
            removeJob(id);
        } catch (Exception e) {
            log.error("Remove job failed", e);
        }
        return removeById(id);
    }

    public boolean toggleEnabled(Long id, Boolean enabled) {
        BackupTask task = getById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setEnabled(enabled);
        updateById(task);
        try {
            removeJob(id);
            if (Boolean.TRUE.equals(enabled) && task.getCronExpression() != null) {
                scheduleJob(task);
            }
        } catch (Exception e) {
            log.error("Toggle job failed", e);
        }
        return true;
    }

    private void scheduleJob(BackupTask task) throws SchedulerException {
        String jobKey = "backup_" + task.getId();
        JobDetail jobDetail = JobBuilder.newJob(com.backup.server.task.BackupJob.class)
                .withIdentity(jobKey)
                .usingJobData("taskId", task.getId())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobKey + "_trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Scheduled backup task: {}, cron: {}", task.getName(), task.getCronExpression());
    }

    private void removeJob(Long taskId) throws SchedulerException {
        String jobKey = "backup_" + taskId;
        JobKey key = new JobKey(jobKey);
        if (scheduler.checkExists(key)) {
            scheduler.deleteJob(key);
        }
    }
}
