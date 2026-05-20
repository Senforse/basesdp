package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.entity.SysMenu;
import org.nmgyj.system.service.SysMenuService;
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
 * 后台菜单 CRUD 与树形查询。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/admin/menus")
@Tag(name = "菜单管理")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * @param sysMenuService 菜单领域服务
     */
    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    /**
     * 查询全部菜单并以树形返回（实体字段含 children）。
     *
     * @return 菜单树根节点列表
     */
    @Operation(summary = "菜单树（管理端）")
    @GetMapping("/tree")
    @SaCheckPermission("system:menu:list")
    public ApiResponse<List<SysMenu>> adminTree() {
        return ApiResponse.success(sysMenuService.buildAdminEntityTree());
    }

    /**
     * 新增菜单。
     *
     * @param body 菜单实体
     * @return 操作结果
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    @SaCheckPermission("system:menu:edit")
    public ApiResponse<Void> create(@RequestBody SysMenu body) {
        try {
            sysMenuService.save(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新菜单。
     *
     * @param id   菜单主键
     * @param body 菜单实体
     * @return 操作结果
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:edit")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysMenu body) {
        try {
            body.setId(id);
            sysMenuService.update(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除菜单（存在子菜单时不允许删除）。
     *
     * @param id 菜单主键
     * @return 操作结果；失败时 message 说明原因
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:edit")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        try {
            sysMenuService.delete(id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }
}
