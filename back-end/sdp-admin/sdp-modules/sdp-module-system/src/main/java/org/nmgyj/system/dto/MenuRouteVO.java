package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端动态路由树节点（与 Vue Router 等对接）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "菜单路由节点")
public class MenuRouteVO {

    @Schema(description = "路由 path", example = "/system/user")
    private String path;

    @Schema(description = "视图组件路径，目录可为空", example = "system/user/index")
    private String component;

    @Schema(description = "路由 meta")
    private MenuMetaVO meta;

    @Schema(description = "子路由")
    private List<MenuRouteVO> children = new ArrayList<>();

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
     * @return meta 信息
     */
    public MenuMetaVO getMeta() {
        return meta;
    }

    /**
     * @param meta meta 信息
     */
    public void setMeta(MenuMetaVO meta) {
        this.meta = meta;
    }

    /**
     * @return 子节点列表
     */
    public List<MenuRouteVO> getChildren() {
        return children;
    }

    /**
     * @param children 子节点列表
     */
    public void setChildren(List<MenuRouteVO> children) {
        this.children = children;
    }
}
