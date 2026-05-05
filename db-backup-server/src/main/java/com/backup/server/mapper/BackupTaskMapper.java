package com.backup.server.mapper;

import com.backup.server.entity.BackupTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupTaskMapper extends BaseMapper<BackupTask> {
}
