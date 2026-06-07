package org.xcyms.service;

import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.SystemMonitorDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统服务接口
 */
public interface ISystemService {
    /**
     * 获取所有接口列表
     */
    ApiResult<List<Map<String, Object>>> getEndpoints();

    /**
     * 清除并重新加载接口文档缓存
     */
    void refreshEndpoints();

    /**
     * 获取系统监控信息
     */
    ApiResult<SystemMonitorDTO> getSystemMonitorInfo();
}