package com.backup.server.service;

import com.backup.server.entity.NfsConfig;
import com.backup.server.mapper.NfsConfigMapper;
import com.backup.server.util.NfsMountUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NfsConfigService extends ServiceImpl<NfsConfigMapper, NfsConfig> {

    public Page<NfsConfig> pageList(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<NfsConfig> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(NfsConfig::getName, keyword);
        }
        wrapper.orderByDesc(NfsConfig::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<NfsConfig> listAll() {
        return list(new LambdaQueryWrapper<NfsConfig>().orderByDesc(NfsConfig::getCreateTime));
    }

    public boolean mountNfs(Long id) {
        NfsConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("NFS配置不存在");
        }
        boolean success = NfsMountUtil.mount(config.getServer(), config.getRemotePath(),
                config.getLocalMountPoint(), config.getOptions());
        if (success) {
            config.setMounted(true);
            updateById(config);
        }
        return success;
    }

    public boolean unmountNfs(Long id) {
        NfsConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("NFS配置不存在");
        }
        boolean success = NfsMountUtil.unmount(config.getLocalMountPoint());
        if (success) {
            config.setMounted(false);
            updateById(config);
        }
        return success;
    }

    public boolean checkMounted(Long id) {
        NfsConfig config = getById(id);
        if (config == null) {
            return false;
        }
        boolean mounted = NfsMountUtil.isMounted(config.getLocalMountPoint());
        if (mounted != Boolean.TRUE.equals(config.getMounted())) {
            config.setMounted(mounted);
            updateById(config);
        }
        return mounted;
    }
}
