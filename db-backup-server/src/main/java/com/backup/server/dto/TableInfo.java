package com.backup.server.dto;

import lombok.Data;

@Data
public class TableInfo {

    private String tableName;

    /** 数据大小（字节） */
    private Long dataSize;

    /** 格式化后的大小字符串 */
    private String dataSizeStr;
}
