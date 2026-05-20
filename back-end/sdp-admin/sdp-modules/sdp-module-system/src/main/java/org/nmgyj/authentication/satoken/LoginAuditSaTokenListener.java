package org.nmgyj.authentication.satoken;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import jakarta.servlet.http.HttpServletRequest;
import org.nmgyj.authentication.entity.SysLoginLog;
import org.nmgyj.authentication.mapper.SysLoginLogMapper;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 监听登录、登出与踢下线事件并异步风格写入审计表（失败不影响主流程）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Component
public class LoginAuditSaTokenListener extends SaTokenListenerForSimple {

    private final SysLoginLogMapper sysLoginLogMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * @param sysLoginLogMapper 登录日志 Mapper
     * @param sysUserMapper     用户 Mapper
     */
    public LoginAuditSaTokenListener(SysLoginLogMapper sysLoginLogMapper, SysUserMapper sysUserMapper) {
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * @param loginType       登录类型
     * @param loginId         用户主键
     * @param tokenValue      令牌值
     * @param loginParameter  登录参数
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        Long uid = Long.valueOf(loginId.toString());
        SysUser user = sysUserMapper.selectById(uid);
        insert(uid, user != null ? user.getUsername() : null, 1, 1, "登录成功");
    }

    /**
     * @param loginType   登录类型
     * @param loginId     用户主键，可能为 null
     * @param tokenValue  令牌值
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        Long uid = loginId == null ? null : Long.valueOf(loginId.toString());
        SysUser user = uid == null ? null : sysUserMapper.selectById(uid);
        String username = user != null ? user.getUsername() : (loginId != null ? loginId.toString() : null);
        insert(uid, username, 1, 2, "退出登录");
    }

    /**
     * @param loginType   登录类型
     * @param loginId     用户主键，可能为 null
     * @param tokenValue  令牌值
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        Long uid = loginId == null ? null : Long.valueOf(loginId.toString());
        SysUser user = uid == null ? null : sysUserMapper.selectById(uid);
        insert(uid, user != null ? user.getUsername() : null, 0, 2, "被踢下线");
    }

    private void insert(Long userId, String username, int loginStatus, int logType, String remark) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginIp(resolveClientIp());
        log.setUserAgent(resolveUserAgent());
        log.setLoginStatus(loginStatus);
        log.setLogType(logType);
        log.setRemark(remark);
        log.setLoginTime(LocalDateTime.now());
        try {
            sysLoginLogMapper.insert(log);
        } catch (Exception ignored) {
            // 避免审计日志影响主流程
        }
    }

    private String resolveClientIp() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletRequestAttributes)) {
            return "";
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "";
    }

    private String resolveUserAgent() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletRequestAttributes)) {
            return "";
        }
        String ua = servletRequestAttributes.getRequest().getHeader("User-Agent");
        return ua != null ? ua : "";
    }
}
