package org.xcyms.entity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 首页统计数据传输对象
 */
@Data
public class DashboardStatsDTO {
    // 基础统计
    private Long userCount;
    private Long fileCount;
    private Long logCount;
    private Long totalStorageSize; // 字节

    // 近期趋势 (近7天)
    private List<String> dateLabels;
    private List<Long> fileUploadTrend;
    private List<Long> userActiveTrend;

    // 文件类型分布
    private List<Map<String, Object>> fileTypeDistribution;
}
