package com.backup.server.mapper;

import com.backup.server.entity.BackupHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BackupHistoryMapper extends BaseMapper<BackupHistory> {

    @Select("SELECT * FROM t_backup_history WHERE task_id = #{taskId} AND create_time < #{beforeTime}")
    List<BackupHistory> selectOldBackups(@Param("taskId") Long taskId, @Param("beforeTime") LocalDateTime beforeTime);
}
