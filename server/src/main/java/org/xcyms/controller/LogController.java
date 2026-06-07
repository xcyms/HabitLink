package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.SysLog;
import org.xcyms.entity.dto.LogDTO;
import org.xcyms.service.ISysLogService;

@ApiDoc("日志管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final ISysLogService sysLogService;

    @ApiDoc(value = "分页查询操作日志", notes = "管理员分页查看操作日志列表")
    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<IPage<LogDTO>> getPage(Page<SysLog> page, @RequestBody LogDTO logDTO) {
        return ApiResult.success(sysLogService.getLogPage(page, logDTO));
    }
}
