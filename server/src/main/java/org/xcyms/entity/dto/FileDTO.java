package org.xcyms.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件信息 DTO
 * </p>
 *
 * @author liu-xu
 */
@Data
public class FileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    /**
     * 文件所属业务id
     */
    private Long businessId;

    /**
     * 所属业务类型
     */
    private String businessType;

    /**
     * 服务器路径
     */
    private String url;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 排序
     */
    private Integer sorted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
