package com.backup.server.controller;

import com.backup.server.dto.R;
import com.backup.server.entity.BackupTask;
import com.backup.server.service.BackupExecuteService;
import com.backup.server.service.BackupTaskService;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/task")
public class BackupTaskController {

    @Autowired
    private BackupTaskService backupTaskService;
    @Autowired
    private BackupExecuteService backupExecuteService;

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") Long pageNum,
                     @RequestParam(defaultValue = "10") Long pageSize,
                     @RequestParam(required = false) String keyword) {
        return R.ok(backupTaskService.pageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public R<BackupTask> getById(@PathVariable Long id) {
        return R.ok(backupTaskService.getById(id));
    }

    @PostMapping
    public R<?> save(@RequestBody BackupTask task) {
        backupTaskService.addTask(task);
        return R.ok();
    }

    @PutMapping
    public R<?> update(@RequestBody BackupTask task) {
        backupTaskService.updateTask(task);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        backupTaskService.deleteTask(id);
        return R.ok();
    }

    @PostMapping("/{id}/toggle")
    public R<?> toggle(@PathVariable Long id, @RequestParam Boolean enabled) {
        backupTaskService.toggleEnabled(id, enabled);
        return R.ok();
    }

    @PostMapping("/{id}/execute")
    public R<?> execute(@PathVariable Long id) {
        new Thread(() -> backupExecuteService.executeBackup(id)).start();
        return R.ok("备份任务已启动");
    }

    @GetMapping("/cron/next")
    public R<?> cronNextTimes(@RequestParam String expression, @RequestParam(defaultValue = "5") Integer times) {
        try {
            CronExpression cron = new CronExpression(expression);
            List<String> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date next = new Date();
            for (int i = 0; i < times; i++) {
                next = cron.getNextValidTimeAfter(next);
                if (next == null) break;
                result.add(sdf.format(next));
                next = new Date(next.getTime() + 1000);
            }
            return R.ok(result);
        } catch (ParseException e) {
            return R.error("Cron表达式格式错误: " + e.getMessage());
        }
    }
}
