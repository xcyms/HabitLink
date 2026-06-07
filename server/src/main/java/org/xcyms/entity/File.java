package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件信息表
 * </p>
 *
 * @author liu-xu
 * @since 2026-03-15
 */
@Getter
@Setter
@TableName("sys_file")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /**
     * 文件所属业务id
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 所属业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 服务器路径
     */
    @TableField("url")
    private String url;

    /**
     * 文件名
     */
    @TableField("name")
    private String name;

    /**
     * 文件大小
     */
    @TableField("size")
    private Long size;

    /**
     * 文件类型
     */
    @TableField("type")
    private String type;

    /**
     * 排序
     */
    @TableField("sorted")
    private Integer sorted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标识
     */
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}
