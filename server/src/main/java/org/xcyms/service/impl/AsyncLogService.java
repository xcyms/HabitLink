package org.xcyms.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xcyms.common.Constant;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.SysLog;
import org.xcyms.service.ISysLogService;
import org.xcyms.utils.JsonUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * <p>
 * 异步日志服务实现类
 * </p>
 *
 * @author liu-xu
 * @date 2026年05月18日 11:04
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncLogService {

    private final ISysLogService sysLogService;

    @Async("commonExecutor")
    @CacheEvict(value = Constant.Cache.STATS, key = "'dashboard'")
    public void saveLog(ProceedingJoinPoint joinPoint, long duration, Object result,
                        int status, String errorMsg, Long userId, String username, HttpServletRequest request) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLog sysLog = new SysLog();
        Log logAnnotation = method.getAnnotation(Log.class);
        if (logAnnotation != null) {
            sysLog.setOperation(logAnnotation.value());
        }

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");


        String requestUrl = null;
        if (request != null) {
            requestUrl = request.getRequestURI();
            sysLog.setUrl(requestUrl);
            sysLog.setIp(getIpAddr(request));
        }

        Object[] args = joinPoint.getArgs();
        if ("/api/file/upload".equals(requestUrl)) {
            sysLog.setParams("file upload");
        } else {
            try {
                String params = JsonUtils.toJson(args);
                sysLog.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            } catch (Exception e) {
                sysLog.setParams("serialize params error");
            }
        }

        if (result != null) {
            try {
                String resultStr = JsonUtils.toJson(result);
                sysLog.setResult(resultStr.length() > 2000 ? resultStr.substring(0, 2000) : resultStr);
            } catch (Exception e) {
                sysLog.setResult("serialize result error");
            }
        }

        sysLog.setDuration(duration);
        sysLog.setStatus(status);
        sysLog.setErrorMsg(errorMsg);
        sysLog.setCreateTime(LocalDateTime.now());
        sysLog.setUserId(userId);
        sysLog.setUsername(username);

        sysLogService.save(sysLog);
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
