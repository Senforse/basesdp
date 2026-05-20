package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.system.entity.SysRole;
import org.nmgyj.system.entity.SysRoleMenu;
import org.nmgyj.system.entity.SysUserRole;
import org.nmgyj.system.mapper.SysRoleMapper;
import org.nmgyj.system.mapper.SysRoleMenuMapper;
import org.nmgyj.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色维护及角色与菜单关联维护。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * @param sysRoleMapper       角色 Mapper
     * @param sysRoleMenuMapper   角色菜单关联 Mapper
     * @param sysUserRoleMapper   用户角色关联 Mapper
     */
    public SysRoleService(
            SysRoleMapper sysRoleMapper,
            SysRoleMenuMapper sysRoleMenuMapper,
            SysUserRoleMapper sysUserRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    /**
     * 全部角色列表。
     *
     * @return 角色集合
     */
    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder));
    }

    /**
     * 按主键查询角色。
     *
     * @param id 角色主键
     * @return 实体或 null
     */
    public SysRole get(Long id) {
        return sysRoleMapper.selectById(id);
    }

    /**
     * 新增角色。
     *
     * @param role 角色实体
     * @throws IllegalArgumentException 编码或名称为空
     */
    public void save(SysRole role) {
        validate(role);
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        sysRoleMapper.insert(role);
    }

    /**
     * 更新角色。
     *
     * @param role 含主键的实体
     * @throws IllegalArgumentException 编码或名称为空
     */
    public void update(SysRole role) {
        validate(role);
        sysRoleMapper.updateById(role);
    }

    /**
     * 删除角色及其关联。
     *
     * @param id 角色主键
     * @throws IllegalArgumentException 试图删除超级管理员角色时
     */
    public void delete(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role != null && RbacPermissionService.SUPER_ROLE_CODE.equals(role.getRoleCode())) {
            throw new IllegalArgumentException("不允许删除超级管理员角色");
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        sysRoleMapper.deleteById(id);
    }

    /**
     * 为角色重新绑定菜单（先清空再插入）。
     *
     * @param roleId  角色主键
     * @param menuIds 菜单主键列表，可为空表示清空
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
    }

    /**
     * 查询角色已绑定菜单 ID。
     *
     * @param roleId 角色主键
     * @return 菜单 ID 列表
     */
    public List<Long> listMenuIds(Long roleId) {
        return sysRoleMenuMapper
                .selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    private void validate(SysRole role) {
        if (!StringUtils.hasText(role.getRoleCode()) || !StringUtils.hasText(role.getRoleName())) {
            throw new IllegalArgumentException("角色编码与名称不能为空");
        }
    }
}
