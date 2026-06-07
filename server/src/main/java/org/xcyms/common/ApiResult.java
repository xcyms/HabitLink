package org.xcyms.common;

import lombok.Data;

/**
 * <p>
 *     公共返回结果类
 * </p>
 * @author liu-xu
 * @date 2026年01月11日 11:37
 */
@Data
public class ApiResult<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> ApiResult<T> success() {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(200);
        r.setMessage("操作成功");
        return r;
    }

    public static <T> ApiResult<T> error(String msg) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(500);
        r.setMessage(msg);
        return r;
    }

    public static <T> ApiResult<T> error(Integer code, String msg) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }
}
