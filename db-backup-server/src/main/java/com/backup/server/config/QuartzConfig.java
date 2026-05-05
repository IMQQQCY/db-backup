package com.backup.server.config;

import com.backup.server.task.BackupJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail backupJobDetail() {
        return JobBuilder.newJob(BackupJob.class)
                .withIdentity("backupJob")
                .storeDurably()
                .build();
    }
}
