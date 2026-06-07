package org.xcyms.aspect;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xcyms.service.impl.AsyncLogService;

/**
 * 操作日志切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final AsyncLogService asyncLogService;

    @Around("@annotation(org.xcyms.common.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 1. 尝试预先获取用户信息 (适用于普通操作和退出登录)
        Long userId = null;
        String username = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                username = (String) StpUtil.getSession().get("username");
            }
        } catch (Exception e) {
            // 预取失败忽略，可能是未登录状态
        }

        Object result = null;
        int status = 1;
        String errorMsg = null;

        try {
            result = point.proceed();
            
            // 2. 如果执行后发现 userId 仍为空，尝试再次获取 (适用于登录操作)
            if (userId == null) {
                try {
                    if (StpUtil.isLogin()) {
                        userId = StpUtil.getLoginIdAsLong();
                        username = (String) StpUtil.getSession().get("username");
                    }
                } catch (Exception e) {
                    // 再次获取失败忽略
                }
            }
            
            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - beginTime;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            asyncLogService.saveLog(point, duration, result, status, errorMsg, userId, username, request);
        }
    }
}
