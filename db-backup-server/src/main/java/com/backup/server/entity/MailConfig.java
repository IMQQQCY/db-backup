package com.backup.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_mail_config")
public class MailConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String smtpHost;

    private Integer smtpPort;

    private String username;

    private String password;

    private Boolean enableSsl;

    private String fromAddress;

    private String toAddresses;

    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
