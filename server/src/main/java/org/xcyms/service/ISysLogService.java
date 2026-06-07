package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.entity.SysLog;
import org.xcyms.entity.dto.LogDTO;

/**
 * 日志服务接口
 */
public interface ISysLogService extends IService<SysLog> {
    /**
     * 分页查询日志
     */
    IPage<LogDTO> getLogPage(Page<SysLog> page, LogDTO logDTO);
}
