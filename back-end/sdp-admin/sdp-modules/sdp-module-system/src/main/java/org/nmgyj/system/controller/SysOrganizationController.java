package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.entity.SysOrganization;
import org.nmgyj.system.service.SysOrganizationService;
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
 * 组织架构树形维护接口。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/admin/orgs")
@Tag(name = "组织管理")
public class SysOrganizationController {

    private final SysOrganizationService sysOrganizationService;

    /**
     * @param sysOrganizationService 组织领域服务
     */
    public SysOrganizationController(SysOrganizationService sysOrganizationService) {
        this.sysOrganizationService = sysOrganizationService;
    }

    /**
     * 组织树。
     *
     * @return 树根节点列表
     */
    @Operation(summary = "组织树")
    @GetMapping("/tree")
    @SaCheckPermission("system:org:list")
    public ApiResponse<List<SysOrganization>> tree() {
        return ApiResponse.success(sysOrganizationService.tree());
    }

    /**
     * 组织详情。
     *
     * @param id 组织主键
     * @return 实体，可能为 null
     */
    @Operation(summary = "组织详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:org:list")
    public ApiResponse<SysOrganization> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(sysOrganizationService.get(id));
    }

    /**
     * 新增组织。
     *
     * @param body 组织实体
     * @return 操作结果
     */
    @Operation(summary = "新增组织")
    @PostMapping
    @SaCheckPermission("system:org:edit")
    public ApiResponse<Void> create(@RequestBody SysOrganization body) {
        try {
            sysOrganizationService.save(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 更新组织。
     *
     * @param id   组织主键
     * @param body 组织实体
     * @return 操作结果
     */
    @Operation(summary = "更新组织")
    @PutMapping("/{id}")
    @SaCheckPermission("system:org:edit")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysOrganization body) {
        try {
            body.setId(id);
            sysOrganizationService.update(body);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 删除组织（存在子组织时不允许删除）。
     *
     * @param id 组织主键
     * @return 操作结果
     */
    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:org:edit")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        try {
            sysOrganizationService.delete(id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }
}
