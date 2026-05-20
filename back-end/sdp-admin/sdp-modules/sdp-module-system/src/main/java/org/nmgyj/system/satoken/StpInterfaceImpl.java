package org.nmgyj.system.satoken;

import cn.dev33.satoken.stp.StpInterface;
import org.nmgyj.system.service.RbacPermissionService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 为 Sa-Token 提供当前登录用户的权限码与角色编码列表。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final RbacPermissionService rbacPermissionService;

    /**
     * @param rbacPermissionService RBAC 查询服务
     */
    public StpInterfaceImpl(RbacPermissionService rbacPermissionService) {
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * @param loginId   登录主键（数值字符串）
     * @param loginType 登录类型标识（Sa-Token）
     * @return 权限标识列表；超级管理员包含 {@code *}
     * @throws NumberFormatException {@code loginId} 无法解析为 Long 时
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return rbacPermissionService.listPermissionCodes(Long.valueOf(loginId.toString()));
    }

    /**
     * @param loginId   登录主键（数值字符串）
     * @param loginType 登录类型标识（Sa-Token）
     * @return 角色编码列表
     * @throws NumberFormatException {@code loginId} 无法解析为 Long 时
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return rbacPermissionService.listRoleCodes(Long.valueOf(loginId.toString()));
    }
}
