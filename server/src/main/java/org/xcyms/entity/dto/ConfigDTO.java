package org.xcyms.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDateTime;

/**
 * <p>
 *     系统配置DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 11:06
 */
@Data
public class ConfigDTO {

    @ApiDocProperty("配置ID")
    private Long id;

    @ApiDocProperty("用户ID (NULL为系统默认)")
    private Long userId;

    @ApiDocProperty("配置键 (如: max_file_size)")
    @NotNull(message = "配置键不能为空")
    private String configKey;

    @ApiDocProperty("配置值")
    @NotNull(message = "配置值不能为空")
    private String configValue;

    @ApiDocProperty("配置名称")
    @NotNull(message = "配置名称不能为空")
    private String configName;

    @ApiDocProperty("备注")
    private String remark;

    @ApiDocProperty("创建时间")
    private LocalDateTime createTime;

    @ApiDocProperty("更新时间")
    private LocalDateTime updateTime;
}