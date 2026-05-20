package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 为角色批量绑定菜单的请求体。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "角色分配菜单请求")
public class RoleAssignMenusRequest {

    @Schema(description = "菜单主键 ID 列表", example = "[1, 2, 3]")
    private List<Long> menuIds = new ArrayList<>();

    /**
     * @return 菜单 ID 列表
     */
    public List<Long> getMenuIds() {
        return menuIds;
    }

    /**
     * @param menuIds 菜单 ID 列表
     */
    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
