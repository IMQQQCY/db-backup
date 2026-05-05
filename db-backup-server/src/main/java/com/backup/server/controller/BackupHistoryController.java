package com.backup.server.controller;

import com.backup.server.dto.R;
import com.backup.server.service.BackupHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class BackupHistoryController {

    @Autowired
    private BackupHistoryService backupHistoryService;

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") Long pageNum,
                     @RequestParam(defaultValue = "10") Long pageSize,
                     @RequestParam(required = false) Long taskId) {
        return R.ok(backupHistoryService.pageList(pageNum, pageSize, taskId));
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        backupHistoryService.removeById(id);
        return R.ok();
    }
}
