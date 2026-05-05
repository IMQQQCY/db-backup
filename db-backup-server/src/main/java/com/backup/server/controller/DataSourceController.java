package com.backup.server.controller;

import com.backup.server.dto.R;
import com.backup.server.entity.DataSource;
import com.backup.server.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/page")
    public R<?> page(@RequestParam(defaultValue = "1") Long pageNum,
                     @RequestParam(defaultValue = "10") Long pageSize,
                     @RequestParam(required = false) String keyword) {
        return R.ok(dataSourceService.pageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/list")
    public R<List<DataSource>> list() {
        return R.ok(dataSourceService.listAll());
    }

    @GetMapping("/{id}")
    public R<DataSource> getById(@PathVariable Long id) {
        return R.ok(dataSourceService.getById(id));
    }

    @PostMapping
    public R<?> save(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return R.ok();
    }

    @PutMapping
    public R<?> update(@RequestBody DataSource dataSource) {
        dataSourceService.updateById(dataSource);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        dataSourceService.removeById(id);
        return R.ok();
    }

    @PostMapping("/test")
    public R<Boolean> testConnection(@RequestBody DataSource dataSource) {
        return R.ok(dataSourceService.testConnection(dataSource));
    }

    @GetMapping("/{id}/tables")
    public R<List<String>> getTables(@PathVariable Long id) {
        return R.ok(dataSourceService.getTableList(id));
    }

    @GetMapping("/{id}/tables/page")
    public R<Map<String, Object>> getTablesPage(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "20") Integer pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "name") String orderBy,
                                                @RequestParam(defaultValue = "asc") String orderDir) {
        return R.ok(dataSourceService.getTableInfoPage(id, keyword, orderBy, orderDir, pageNum, pageSize));
    }
}
