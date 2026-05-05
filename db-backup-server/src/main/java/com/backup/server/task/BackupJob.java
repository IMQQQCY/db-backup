package com.backup.server.task;

import com.backup.server.service.BackupExecuteService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackupJob implements Job {

    @Autowired
    private BackupExecuteService backupExecuteService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long taskId = dataMap.getLong("taskId");
        log.info("Quartz job execute backup task: {}", taskId);
        try {
            backupExecuteService.executeBackup(taskId);
        } catch (Exception e) {
            log.error("Quartz job execute failed: {}", taskId, e);
        }
    }
}
