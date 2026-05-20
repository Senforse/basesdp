package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.dto.UserAssignRolesRequest;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.service.SysUserAdminService;
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
 * 后台用户 CRUD 及用户角色分配。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "用户管理")
public class SysUserAdminController {

    private final SysUserAdminService sysUserAdminService;

    /**
     * @param sysUserAdminService 用户维护服务
     */
    public SysUserAdminController(SysUserAdminService sysUserAdminService) {
        this.sysUserAdminService = sysUserAdminService;
    }

    /**
     * 用户列表。
     *
     * @return 用户集合（密码字段默认不回传）
     */
    @Operation(summary = "用户列表")
    @GetMapping
    @SaCheckPermission("system:user:list")
    public ApiResponse<List<SysUser>> list() {
        return ApiResponse.success(sysUserAdminService.listUsers());
    }

    /**
     * 用户详情。
     *
     * @param id 用户主键
     * @return 用户实体
     */
    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:user:list")
    public ApiResponse<SysUser> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(sysUserAdminService.get(id));
    }

    /**
     * 创建用户。
     *
     * @param body 用户实体（须含用户名与初始密码）
     * @return 操作结果；用户名重复等返回失败响应
     */
    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> create(@RequestBody SysUser body) {
        try {
            sysUserAdminService.create(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新用户；密码为空则保留原哈希。
     *
     * @param id   用户主键
     * @param body 变更字段
     * @return 操作结果
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysUser body) {
        try {
            sysUserAdminService.update(id, body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除用户及其角色关联。
     *
     * @param id 用户主键
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        sysUserAdminService.delete(id);
        return ApiResponse.success(null);
    }

    /**
     * 查询用户已绑定角色 ID。
     *
     * @param id 用户主键
     * @return 角色 ID 列表
     */
    @Operation(summary = "用户已分配角色 ID")
    @GetMapping("/{id}/roles")
    @SaCheckPermission("system:user:list")
    public ApiResponse<List<Long>> roleIds(@PathVariable("id") Long id) {
        return ApiResponse.success(sysUserAdminService.listRoleIds(id));
    }

    /**
     * 为用户分配角色（全量覆盖）。
     *
     * @param id   用户主键
     * @param body 角色 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "用户分配角色")
    @PutMapping("/{id}/roles")
    @SaCheckPermission("system:user:assign")
    public ApiResponse<Void> assignRoles(@PathVariable("id") Long id, @RequestBody UserAssignRolesRequest body) {
        sysUserAdminService.assignRoles(id, body.getRoleIds());
        return ApiResponse.success(null);
    }
}
