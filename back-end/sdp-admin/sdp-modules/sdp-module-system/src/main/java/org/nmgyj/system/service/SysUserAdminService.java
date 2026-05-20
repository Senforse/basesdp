package org.nmgyj.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.authentication.audit.AuditLog;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.entity.SysUserRole;
import org.nmgyj.system.mapper.SysUserMapper;
import org.nmgyj.system.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台用户的增删改查及角色分配。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class SysUserAdminService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param sysUserMapper       用户 Mapper
     * @param sysUserRoleMapper   用户角色关联 Mapper
     * @param passwordEncoder     密码加密
     */
    public SysUserAdminService(
            SysUserMapper sysUserMapper, SysUserRoleMapper sysUserRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户列表（按 ID 倒序）。
     *
     * @return 用户集合
     */
    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getId));
    }

    /**
     * 用户详情。
     *
     * @param id 用户主键
     * @return 实体或 null
     */
    public SysUser get(Long id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 创建用户。
     *
     * @param user 用户信息（须含用户名与明文密码）
     * @throws IllegalArgumentException 校验失败或用户名重复
     */
    @AuditLog(action = "CREATE_USER", bizType = "sys_user", bizIdSpEL = "#user.id", recordDiff = true)
    public void create(SysUser user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        long cnt = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (cnt > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        sysUserMapper.insert(user);
    }

    /**
     * 更新用户；若密码字段非空则重新加密。
     *
     * @param id   用户主键
     * @param patch 变更字段
     * @throws IllegalArgumentException 用户不存在
     */
    @AuditLog(action = "UPDATE_USER", bizType = "sys_user", bizIdSpEL = "#id", recordDiff = true)
    public void update(Long id, SysUser patch) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        patch.setId(id);
        if (StringUtils.hasText(patch.getPassword())) {
            patch.setPassword(passwordEncoder.encode(patch.getPassword()));
        } else {
            patch.setPassword(existing.getPassword());
        }
        sysUserMapper.updateById(patch);
    }

    /**
     * 删除用户及其角色关联。
     *
     * @param id 用户主键
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(action = "DELETE_USER", bizType = "sys_user", bizIdSpEL = "#id", recordDiff = false)
    public void delete(Long id) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        sysUserMapper.deleteById(id);
    }

    /**
     * 为用户重新绑定角色。
     *
     * @param userId  用户主键
     * @param roleIds 角色主键列表，可为空表示清空
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(action = "ASSIGN_USER_ROLE", bizType = "sys_user_role", bizIdSpEL = "#userId", recordDiff = true)
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleMapper.insert(ur);
        }
    }

    /**
     * 查询用户已绑定角色 ID。
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
}
