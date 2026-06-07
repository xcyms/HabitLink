package org.xcyms.service;

import org.xcyms.entity.dto.DashboardStatsDTO;

/**
 * 统计服务接口
 */
public interface IStatsService {
    /**
     * 获取首页统计数据
     */
    DashboardStatsDTO getDashboardStats();
}
