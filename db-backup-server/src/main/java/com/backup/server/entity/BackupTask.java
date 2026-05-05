package com.backup.server.entity;

import com.backup.server.enums.BackupContent;
import com.backup.server.enums.BackupType;
import com.backup.server.enums.StorageType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_backup_task")
public class BackupTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long dataSourceId;

    private BackupType backupType;

    private BackupContent backupContent;

    private String tableList;

    private StorageType storageType;

    private String storagePath;

    private Long nfsConfigId;

    private String cronExpression;

    private Boolean enabled;

    private Integer retainDays;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
