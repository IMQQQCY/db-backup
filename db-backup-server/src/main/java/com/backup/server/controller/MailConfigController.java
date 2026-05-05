package com.backup.server.controller;

import com.backup.server.dto.R;
import com.backup.server.entity.MailConfig;
import com.backup.server.service.MailConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class MailConfigController {

    @Autowired
    private MailConfigService mailConfigService;

    @GetMapping
    public R<MailConfig> get() {
        MailConfig config = mailConfigService.getActiveConfig();
        if (config == null) {
            config = new MailConfig();
            config.setSmtpPort(587);
            config.setEnableSsl(true);
            config.setEnabled(false);
        }
        return R.ok(config);
    }

    @PostMapping
    public R<?> save(@RequestBody MailConfig config) {
        mailConfigService.saveOrUpdate(config);
        return R.ok();
    }

    @PostMapping("/test")
    public R<Boolean> test(@RequestBody MailConfig config) {
        return R.ok(mailConfigService.testSend(config));
    }
}
