package org.xcyms.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 角色表DTO
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    private Long userCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
