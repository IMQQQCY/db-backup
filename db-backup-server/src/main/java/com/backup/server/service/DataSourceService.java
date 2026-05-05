package com.backup.server.service;

import com.backup.server.dto.TableInfo;
import com.backup.server.entity.DataSource;
import com.backup.server.mapper.DataSourceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSourceService extends ServiceImpl<DataSourceMapper, DataSource> {

    public Page<DataSource> pageList(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<DataSource> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(DataSource::getName, keyword)
                    .or()
                    .like(DataSource::getHost, keyword)
                    .or()
                    .like(DataSource::getDatabaseName, keyword);
        }
        wrapper.orderByDesc(DataSource::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<DataSource> listAll() {
        return list(new LambdaQueryWrapper<DataSource>().orderByDesc(DataSource::getCreateTime));
    }

    public boolean testConnection(DataSource dataSource) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=5000&socketTimeout=5000",
                    dataSource.getHost(), dataSource.getPort(), dataSource.getDatabaseName());
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url,
                    dataSource.getUsername(), dataSource.getPassword())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getTableList(Long dataSourceId) {
        DataSource ds = getById(dataSourceId);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        List<String> tables = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=5000&socketTimeout=5000",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url,
                    ds.getUsername(), ds.getPassword());
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage());
        }
        return tables;
    }

    /**
     * 获取表列表（含大小），支持模糊搜索、排序、分页
     * 使用 information_schema.TABLES 的缓存数据，不影响数据库性能
     */
    public Map<String, Object> getTableInfoPage(Long dataSourceId, String keyword,
                                                 String orderBy, String orderDir,
                                                 int pageNum, int pageSize) {
        DataSource ds = getById(dataSourceId);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        List<TableInfo> allTables = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=5000&socketTimeout=5000",
                    ds.getHost(), ds.getPort(), ds.getDatabaseName());
            String sql = "SELECT TABLE_NAME, (DATA_LENGTH + INDEX_LENGTH) AS DATA_SIZE " +
                    "FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + ds.getDatabaseName() + "'";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url,
                    ds.getUsername(), ds.getPassword());
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    TableInfo info = new TableInfo();
                    info.setTableName(rs.getString("TABLE_NAME"));
                    info.setDataSize(rs.getLong("DATA_SIZE"));
                    info.setDataSizeStr(formatSize(rs.getLong("DATA_SIZE")));
                    allTables.add(info);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表信息失败: " + e.getMessage());
        }

        // 模糊搜索
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            allTables = allTables.stream()
                    .filter(t -> t.getTableName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        // 排序
        Comparator<TableInfo> comparator;
        if ("size".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(TableInfo::getDataSize);
        } else {
            comparator = Comparator.comparing(TableInfo::getTableName);
        }
        if ("desc".equalsIgnoreCase(orderDir)) {
            comparator = comparator.reversed();
        }
        allTables.sort(comparator);

        // 分页
        int total = allTables.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<TableInfo> records = fromIndex < total ? allTables.subList(fromIndex, toIndex) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024L * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
