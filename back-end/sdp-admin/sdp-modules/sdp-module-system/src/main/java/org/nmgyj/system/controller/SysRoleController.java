package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.dto.RoleAssignMenusRequest;
import org.nmgyj.system.entity.SysRole;
import org.nmgyj.system.service.SysRoleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色维护及角色与菜单关联。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/admin/roles")
@Tag(name = "角色管理")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * @param sysRoleService 角色领域服务
     */
    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 角色列表。
     *
     * @return 全部角色
     */
    @Operation(summary = "角色列表")
    @GetMapping
    @SaCheckPermission("system:role:list")
    public ApiResponse<List<SysRole>> list() {
        return ApiResponse.success(sysRoleService.listAll());
    }

    /**
     * 角色详情。
     *
     * @param id 角色主键
     * @return 实体，可能为 null
     */
    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:role:list")
    public ApiResponse<SysRole> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(sysRoleService.get(id));
    }

    /**
     * 新增角色。
     *
     * @param body 角色实体
     * @return 操作结果
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("system:role:edit")
    public ApiResponse<Void> create(@RequestBody SysRole body) {
        try {
            sysRoleService.save(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新角色。
     *
     * @param id   角色主键
     * @param body 角色实体
     * @return 操作结果
     */
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysRole body) {
        try {
            body.setId(id);
            sysRoleService.update(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除角色（超级管理员角色不可删）。
     *
     * @param id 角色主键
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        try {
            sysRoleService.delete(id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 为角色分配菜单（全量覆盖）。
     *
     * @param id   角色主键
     * @param body 菜单 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "角色分配菜单")
    @PutMapping("/{id}/menus")
    @SaCheckPermission("system:role:assign")
    public ApiResponse<Void> assignMenus(@PathVariable("id") Long id, @RequestBody RoleAssignMenusRequest body) {
        sysRoleService.assignMenus(id, body.getMenuIds());
        return ApiResponse.success(null);
    }

    /**
     * 查询角色已绑定菜单 ID。
     *
     * @param id 角色主键
     * @return 菜单 ID 列表
     */
    @Operation(summary = "角色已分配菜单 ID")
    @GetMapping("/{id}/menus")
    @SaCheckPermission("system:role:list")
    public ApiResponse<List<Long>> menuIds(@PathVariable("id") Long id) {
        return ApiResponse.success(sysRoleService.listMenuIds(id));
    }
}
