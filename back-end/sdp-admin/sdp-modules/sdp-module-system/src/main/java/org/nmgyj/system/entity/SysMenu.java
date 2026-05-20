package org.nmgyj.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单与权限资源实体，对应 {@code rbac-dm.sql} 中 {@code sys_menu}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "系统菜单")
@TableName("sys_menu")
public class SysMenu {

    @Schema(description = "主键", example = "10")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "父菜单 ID，根为 0", example = "0")
    @TableField("PARENT_ID")
    private Long parentId;

    @Schema(description = "菜单名称", example = "用户管理")
    @TableField("MENU_NAME")
    private String menuName;

    @Schema(description = "路由 path", example = "/system/user")
    @TableField("PATH")
    private String path;

    @Schema(description = "前端组件", example = "system/user/index")
    @TableField("COMPONENT")
    private String component;

    @Schema(description = "权限标识，逗号分隔", example = "system:user:list")
    @TableField("PERMS")
    private String perms;

    @Schema(description = "类型：0 目录 1 菜单 2 按钮", example = "1")
    @TableField("MENU_TYPE")
    private Integer menuType;

    @Schema(description = "图标", example = "User")
    @TableField("ICON")
    private String icon;

    @Schema(description = "排序，越小越靠前", example = "1")
    @TableField("SORT_ORDER")
    private Integer sortOrder;

    @Schema(description = "是否可见：1 显示 0 隐藏", example = "1")
    @TableField("VISIBLE")
    private Integer visible;

    @Schema(description = "状态：1 正常 0 停用", example = "1")
    @TableField("STATUS")
    private Integer status;

    @Schema(description = "子菜单（树形接口填充）")
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();

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
     * @return 父菜单 ID
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * @param parentId 父菜单 ID
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * @return 菜单名称
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * @param menuName 菜单名称
     */
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    /**
     * @return 路由 path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path 路由 path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return 组件路径
     */
    public String getComponent() {
        return component;
    }

    /**
     * @param component 组件路径
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * @return 权限字符串
     */
    public String getPerms() {
        return perms;
    }

    /**
     * @param perms 权限字符串
     */
    public void setPerms(String perms) {
        this.perms = perms;
    }

    /**
     * @return 菜单类型
     */
    public Integer getMenuType() {
        return menuType;
    }

    /**
     * @param menuType 菜单类型
     */
    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    /**
     * @return 图标
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon 图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return 排序值
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder 排序值
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * @return 是否可见
     */
    public Integer getVisible() {
        return visible;
    }

    /**
     * @param visible 是否可见
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
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

    /**
     * @return 子菜单列表
     */
    public List<SysMenu> getChildren() {
        return children;
    }

    /**
     * @param children 子菜单列表
     */
    public void setChildren(List<SysMenu> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}
