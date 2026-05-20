package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 前端路由 meta 信息（标题、图标等）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "菜单路由 meta")
public class MenuMetaVO {

    @Schema(description = "侧边栏或面包屑标题", example = "用户管理")
    private String title;

    @Schema(description = "图标名称或类名", example = "User")
    private String icon;

    public MenuMetaVO() {
    }

    public MenuMetaVO(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    /**
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
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
}
