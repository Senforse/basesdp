package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.entity.SysMenu;
import org.nmgyj.system.entity.SysRole;
import org.nmgyj.system.entity.SysRoleMenu;
import org.nmgyj.system.entity.SysUserRole;
import org.nmgyj.system.mapper.SysMenuMapper;
import org.nmgyj.system.mapper.SysRoleMapper;
import org.nmgyj.system.mapper.SysRoleMenuMapper;
import org.nmgyj.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于用户-角色-菜单关系计算权限码、菜单可见范围及超级管理员判定。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class RbacPermissionService {

    /** 超级管理员角色编码，拥有全部菜单与 {@code *} 权限。 */
    public static final String SUPER_ROLE_CODE = "SUPER_ADMIN";
    private static final Set<String> SUPER_ROLE_CODE_ALIASES = new HashSet<>(Set.of(
            "SUPER_ADMIN",
            "SUPERADMIN",
            "SUPER-ADMIN",
            "ROOT"
    ));
    private static final String SUPER_ROLE_NAME = "超级管理员";

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    /**
     * @param sysUserRoleMapper   用户角色 Mapper
     * @param sysRoleMapper       角色 Mapper
     * @param sysRoleMenuMapper   角色菜单 Mapper
     * @param sysMenuMapper       菜单 Mapper
     */
    public RbacPermissionService(
            SysUserRoleMapper sysUserRoleMapper,
            SysRoleMapper sysRoleMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            SysMenuMapper sysMenuMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    /**
     * 是否为超级管理员。
     *
     * @param userId 用户主键
     * @return 拥有 {@link #SUPER_ROLE_CODE} 时为 true
     */
    public boolean isSuperAdmin(Long userId) {
        List<Long> roleIds = listRoleIds(userId);
        if (roleIds.isEmpty()) {
            return false;
        }
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        return roles.stream().anyMatch(this::isSuperRole);
    }

    /**
     * 用户拥有的角色主键列表。
     *
     * @param userId 用户主键
     * @return 角色 ID 列表
     */
    public List<Long> listRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    /**
     * 用户拥有的角色编码列表。
     *
     * @param userId 用户主键
     * @return 角色编码列表
     */
    public List<String> listRoleCodes(Long userId) {
        List<Long> roleIds = listRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds).stream().map(SysRole::getRoleCode).filter(StringUtils::hasText).toList();
    }

    /**
     * 用户拥有的权限标识列表（超级管理员返回 {@code ["*"]}）。
     *
     * @param userId 用户主键
     * @return 权限字符串列表
     */
    public List<String> listPermissionCodes(Long userId) {
        if (isSuperAdmin(userId)) {
            return List.of("*");
        }
        List<Long> roleIds = listRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<SysRoleMenu> links = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> menuIds = links.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds);
        Set<String> perms = new LinkedHashSet<>();
        for (SysMenu menu : menus) {
            if (menu == null || menu.getStatus() == null || menu.getStatus() != 1) {
                continue;
            }
            if (!StringUtils.hasText(menu.getPerms())) {
                continue;
            }
            Arrays.stream(menu.getPerms().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(perms::add);
        }
        return new ArrayList<>(perms);
    }

    /**
     * 用户允许访问的菜单 ID 集合（含祖先菜单 ID）。
     *
     * @param userId 用户主键
     * @return 菜单 ID 集合
     */
    public Set<Long> collectAllowedMenuIdsWithAncestors(Long userId) {
        if (isSuperAdmin(userId)) {
            return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()).stream()
                    .map(SysMenu::getId)
                    .collect(Collectors.toSet());
        }
        List<Long> roleIds = listRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        List<SysRoleMenu> links = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> direct = links.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (direct.isEmpty()) {
            return Set.of();
        }
        List<SysMenu> all = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>());
        java.util.Map<Long, SysMenu> byId = all.stream().collect(Collectors.toMap(SysMenu::getId, m -> m, (a, b) -> a));
        Set<Long> expanded = new LinkedHashSet<>(direct);
        for (Long mid : direct) {
            Long pid = resolveParentId(byId, mid);
            while (pid != null && pid > 0 && !expanded.contains(pid)) {
                expanded.add(pid);
                pid = resolveParentId(byId, pid);
            }
        }
        return expanded;
    }

    private Long resolveParentId(java.util.Map<Long, SysMenu> byId, Long id) {
        SysMenu m = byId.get(id);
        if (m == null) {
            return null;
        }
        return m.getParentId();
    }

    private boolean isSuperRole(SysRole role) {
        if (role == null) {
            return false;
        }
        String roleCode = role.getRoleCode() == null ? "" : role.getRoleCode().trim().toUpperCase();
        if (SUPER_ROLE_CODE_ALIASES.contains(roleCode)) {
            return true;
        }
        String roleName = role.getRoleName() == null ? "" : role.getRoleName().trim();
        return SUPER_ROLE_NAME.equals(roleName);
    }
}
