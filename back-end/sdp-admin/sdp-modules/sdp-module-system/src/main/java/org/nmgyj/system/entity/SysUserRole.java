package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户与角色的关联关系，对应表 {@code sys_user_role}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "用户角色关联")
@TableName("sys_user_role")
public class SysUserRole {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户主键", example = "1")
    @TableField("USER_ID")
    private Long userId;

    @Schema(description = "角色主键", example = "2")
    @TableField("ROLE_ID")
    private Long roleId;

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return 角色 ID
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * @param roleId 角色 ID
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
