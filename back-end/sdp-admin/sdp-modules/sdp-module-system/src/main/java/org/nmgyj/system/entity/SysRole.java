package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 角色实体，对应表 {@code sys_role}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "系统角色")
@TableName("sys_role")
public class SysRole {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色编码", example = "ADMIN")
    @TableField("ROLE_CODE")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    @TableField("ROLE_NAME")
    private String roleName;

    @Schema(description = "排序", example = "0")
    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @Schema(description = "状态：1 正常 0 停用", example = "1")
    @TableField("STATUS")
    private Integer status;

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
     * @return 角色编码
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * @param roleCode 角色编码
     */
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    /**
     * @return 角色名称
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName 角色名称
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @return 排序
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder 排序
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
