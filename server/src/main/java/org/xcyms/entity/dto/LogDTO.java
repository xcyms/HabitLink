package org.xcyms.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志数据传输对象
 */
@Data
public class LogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String url;
    private String params;
    private String result;
    private Long duration;
    private String ip;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}
