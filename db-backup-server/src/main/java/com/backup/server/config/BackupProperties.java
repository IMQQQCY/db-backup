package com.backup.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {

    private String tempDir = "./backup-temp";

    private Integer defaultRetainDays = 30;
}
