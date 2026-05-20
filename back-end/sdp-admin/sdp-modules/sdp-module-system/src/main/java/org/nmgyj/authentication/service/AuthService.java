package org.nmgyj.authentication.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.nmgyj.authentication.dto.LoginRequest;
import org.nmgyj.authentication.dto.LoginResponse;
import org.nmgyj.authentication.entity.SysLoginLog;
import org.nmgyj.authentication.mapper.SysLoginLogMapper;
import org.nmgyj.system.constant.OnlineSessionConstants;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.mapper.SysUserMapper;
import org.nmgyj.system.service.RbacPermissionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录校验、令牌签发与登录日志记录。令牌与 Session 由 Sa-Token 经全局 SaTokenDao 写入 Redis（{@code spring.data.redis} + {@code sa-token-redis-jackson}），不在此服务内单独维护在线用户 Redis 结构。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final RbacPermissionService rbacPermissionService;

    /**
     * @param sysUserMapper           用户数据访问
     * @param sysLoginLogMapper       登录日志数据访问
     * @param passwordEncoder         密码匹配（BCrypt）
     * @param rbacPermissionService   角色编码查询
     */
    public AuthService(
            SysUserMapper sysUserMapper,
            SysLoginLogMapper sysLoginLogMapper,
            PasswordEncoder passwordEncoder,
            RbacPermissionService rbacPermissionService) {
        this.sysUserMapper = sysUserMapper;
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.passwordEncoder = passwordEncoder;
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * 校验用户名密码并建立会话，返回前端所需令牌与用户信息。
     *
     * @param request    登录请求
     * @param ip         客户端 IP
     * @param userAgent  User-Agent 原文，可为 null
     * @return 登录响应（含 token、角色等）
     * @throws IllegalArgumentException 凭证无效、用户停用等业务错误
     */
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        String ua = userAgent != null ? userAgent : "";
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            saveLoginLog(null, request == null ? null : request.getUsername(), ip, ua, 0, "用户名或密码为空");
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        Page<SysUser> page = new Page<>(1, 1);
        sysUserMapper.selectPage(page,
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        SysUser user = page.getRecords().isEmpty() ? null : page.getRecords().getFirst();

        if (user == null) {
            saveLoginLog(null, request.getUsername(), ip, ua, 0, "用户不存在");
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            saveLoginLog(user.getId(), request.getUsername(), ip, ua, 0, "用户已停用");
            throw new IllegalArgumentException("账号已停用，请联系管理员");
        }

        boolean match = matches(user.getPassword(), request.getPassword());
        if (!match) {
            saveLoginLog(user.getId(), request.getUsername(), ip, ua, 0, "密码错误");
            throw new IllegalArgumentException("用户名或密码错误");
        }

        StpUtil.login(user.getId());
        // Token-Session 随 SaTokenDao 持久化（启用 Redis-Jackson 时即写入 spring.data.redis）
        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.set(OnlineSessionConstants.APPLICATION, OnlineSessionConstants.DEFAULT_APPLICATION);
        tokenSession.set(OnlineSessionConstants.AUTH_TYPE, "password");
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        response.setTokenHeaderName("satoken");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setRoles(rbacPermissionService.listRoleCodes(user.getId()));
        return response;
    }

    private boolean matches(String dbPassword, String rawPassword) {
        if (!StringUtils.hasText(dbPassword)) {
            return false;
        }
        if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, dbPassword);
        }
        return dbPassword.equals(rawPassword);
    }

    private void saveLoginLog(Long userId, String username, String ip, String userAgent, int status, String remark) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginIp(ip);
        loginLog.setUserAgent(userAgent != null ? userAgent : "");
        loginLog.setLoginStatus(status);
        loginLog.setLogType(1);
        loginLog.setRemark(remark);
        loginLog.setLoginTime(LocalDateTime.now());
        try {
            sysLoginLogMapper.insert(loginLog);
        } catch (Exception ignored) {
            // 登录日志写入失败不应影响主登录流程
        }
    }
}
