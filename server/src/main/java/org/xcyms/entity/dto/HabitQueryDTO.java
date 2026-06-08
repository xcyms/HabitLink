package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

/**
 * 习惯查询 DTO。
 * 用于搜索和后台分页筛选。
 */
@Data
public class HabitQueryDTO {

    @ApiDocProperty("习惯名称关键字")
    private String name;

    @ApiDocProperty("习惯分类编码")
    private String category;

    @ApiDocProperty("习惯状态")
    private Integer status;

    @ApiDocProperty("所属用户ID")
    private Long userId;
}
