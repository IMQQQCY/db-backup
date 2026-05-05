package com.backup.server.service;

import com.backup.server.entity.MailConfig;
import com.backup.server.mapper.MailConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class MailConfigService extends ServiceImpl<MailConfigMapper, MailConfig> {

    @Autowired(required = false)
    private JavaMailSenderImpl mailSender;

    public MailConfig getActiveConfig() {
        LambdaQueryWrapper<MailConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailConfig::getEnabled, true);
        return getOne(wrapper);
    }

    public boolean testSend(MailConfig config) {
        try {
            JavaMailSenderImpl testSender = new JavaMailSenderImpl();
            testSender.setHost(config.getSmtpHost());
            testSender.setPort(config.getSmtpPort());
            testSender.setUsername(config.getUsername());
            testSender.setPassword(config.getPassword());
            testSender.setDefaultEncoding("UTF-8");

            Properties props = testSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            if (Boolean.TRUE.equals(config.getEnableSsl())) {
                props.put("mail.smtp.ssl.enable", "true");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(config.getFromAddress());
            message.setTo(config.getFromAddress());
            message.setSubject("数据库备份服务 - 邮件测试");
            message.setText("这是一封测试邮件，如果您收到说明邮件配置正确。");

            testSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Mail test failed", e);
            return false;
        }
    }

    public void sendNotify(String subject, String content) {
        MailConfig config = getActiveConfig();
        if (config == null) {
            log.warn("No active mail config, skip notify");
            return;
        }
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(config.getFromAddress());
                message.setTo(config.getToAddresses().split(","));
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("Mail sent: {}", subject);
            }
        } catch (Exception e) {
            log.error("Send mail failed", e);
        }
    }
}
