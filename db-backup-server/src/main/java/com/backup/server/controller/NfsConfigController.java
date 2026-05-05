package com.backup.server.controller;

import com.backup.server.dto.R;
import com.backup.server.entity.NfsConfig;
import com.backup.server.service.NfsConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nfs")
public class NfsConfigController {

    @Autowired
    private NfsConfigService nfsConfigService;

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") Long pageNum,
                     @RequestParam(defaultValue = "10") Long pageSize,
                     @RequestParam(required = false) String keyword) {
        return R.ok(nfsConfigService.pageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/list")
    public R<List<NfsConfig>> list() {
        return R.ok(nfsConfigService.listAll());
    }

    @GetMapping("/{id}")
    public R<NfsConfig> getById(@PathVariable Long id) {
        return R.ok(nfsConfigService.getById(id));
    }

    @PostMapping
    public R<?> save(@RequestBody NfsConfig config) {
        nfsConfigService.save(config);
        return R.ok();
    }

    @PutMapping
    public R<?> update(@RequestBody NfsConfig config) {
        nfsConfigService.updateById(config);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        nfsConfigService.removeById(id);
        return R.ok();
    }

    @PostMapping("/{id}/mount")
    public R<?> mount(@PathVariable Long id) {
        boolean success = nfsConfigService.mountNfs(id);
        return success ? R.ok() : R.error("挂载失败");
    }

    @PostMapping("/{id}/unmount")
    public R<?> unmount(@PathVariable Long id) {
        boolean success = nfsConfigService.unmountNfs(id);
        return success ? R.ok() : R.error("卸载失败");
    }

    @GetMapping("/{id}/status")
    public R<Boolean> status(@PathVariable Long id) {
        return R.ok(nfsConfigService.checkMounted(id));
    }
}
