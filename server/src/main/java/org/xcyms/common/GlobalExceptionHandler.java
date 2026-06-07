package org.xcyms.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ApiResult<?> handleNotLoginException(NotLoginException exception) {
        return ApiResult.error(401, "未登录或会话已过期，请重新登录");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ApiResult<?> handleNotPermissionException(NotPermissionException exception) {
        return ApiResult.error(403, "您没有访问该功能的权限");
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResult<?> handleBadRequestException(Exception exception) {
        String message = resolveBadRequestMessage(exception);
        log.warn("请求参数或业务校验失败: {}", message);
        return ApiResult.error(400, message);
    }

    /**
     * 未识别的异常统一按系统错误处理，避免把堆栈细节直接暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception exception) {
        log.error("全局异常捕获", exception);
        return ApiResult.error(500, "系统繁忙，请稍后重试");
    }

    private String resolveBadRequestMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException validException) {
            validException.getBindingResult().getFieldError();
            return validException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof BindException bindException) {
            bindException.getBindingResult().getFieldError();
            return bindException.getBindingResult().getFieldError().getDefaultMessage();
        }
        return exception.getMessage() != null ? exception.getMessage() : "请求参数不合法";
    }
}
