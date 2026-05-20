package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 为用户批量绑定角色的请求体。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "用户分配角色请求")
public class UserAssignRolesRequest {

    @Schema(description = "角色主键 ID 列表", example = "[1, 2]")
    private List<Long> roleIds = new ArrayList<>();

    /**
     * @return 角色 ID 列表
     */
    public List<Long> getRoleIds() {
        return roleIds;
    }

    /**
     * @param roleIds 角色 ID 列表
     */
    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
