package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.xcyms.entity.SysLog;
import org.xcyms.entity.dto.LogDTO;
import org.xcyms.mapper.SysLogMapper;
import org.xcyms.service.ISysLogService;

import java.util.stream.Collectors;

/**
 * 日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

    private final ModelMapper modelMapper;

    @Override
    public IPage<LogDTO> getLogPage(Page<SysLog> page, LogDTO logDTO) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        
        if (logDTO != null) {
            wrapper.like(StringUtils.isNotBlank(logDTO.getUsername()), SysLog::getUsername, logDTO.getUsername())
                   .like(StringUtils.isNotBlank(logDTO.getOperation()), SysLog::getOperation, logDTO.getOperation())
                   .eq(logDTO.getStatus() != null, SysLog::getStatus, logDTO.getStatus());
        }
        
        wrapper.orderByDesc(SysLog::getCreateTime);
        
        IPage<SysLog> logPage = this.page(page, wrapper);
        
        Page<LogDTO> dtoPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        dtoPage.setRecords(logPage.getRecords().stream()
                .map(log -> modelMapper.map(log, LogDTO.class))
                .collect(Collectors.toList()));
        
        return dtoPage;
    }
}
