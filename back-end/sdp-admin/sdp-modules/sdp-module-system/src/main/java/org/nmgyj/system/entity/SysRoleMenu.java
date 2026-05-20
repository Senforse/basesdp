package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 角色与菜单的关联关系，对应表 {@code sys_role_menu}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "角色菜单关联")
@TableName("sys_role_menu")
public class SysRoleMenu {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色主键", example = "1")
    @TableField("ROLE_ID")
    private Long roleId;

    @Schema(description = "菜单主键", example = "10")
    @TableField("MENU_ID")
    private Long menuId;

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

    /**
     * @return 菜单 ID
     */
    public Long getMenuId() {
        return menuId;
    }

    /**
     * @param menuId 菜单 ID
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
