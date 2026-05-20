package org.nmgyj.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.nmgyj.authentication.config.LogArchiveProperties;
import org.nmgyj.authentication.entity.SysApiAccessLog;
import org.nmgyj.authentication.entity.SysAuditLog;
import org.nmgyj.authentication.entity.SysLoginLog;
import org.nmgyj.authentication.mapper.SysApiAccessLogMapper;
import org.nmgyj.authentication.mapper.SysAuditLogMapper;
import org.nmgyj.authentication.mapper.SysLoginLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 历史日志归档清理服务。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Service
public class LogArchiveService {

    private static final Logger log = LoggerFactory.getLogger(LogArchiveService.class);

    private final LogArchiveProperties properties;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final SysApiAccessLogMapper sysApiAccessLogMapper;
    private final SysAuditLogMapper sysAuditLogMapper;

    public LogArchiveService(
            LogArchiveProperties properties,
            SysLoginLogMapper sysLoginLogMapper,
            SysApiAccessLogMapper sysApiAccessLogMapper,
            SysAuditLogMapper sysAuditLogMapper) {
        this.properties = properties;
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.sysApiAccessLogMapper = sysApiAccessLogMapper;
        this.sysAuditLogMapper = sysAuditLogMapper;
    }

    public Map<String, Long> cleanupExpiredLogs() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> result = new LinkedHashMap<>();
        long loginDeleted = cleanupLoginLogs(now.minusDays(Math.max(1, properties.getLoginRetentionDays())));
        long accessDeleted = cleanupAccessLogs(now.minusDays(Math.max(1, properties.getAccessRetentionDays())));
        long auditDeleted = cleanupAuditLogs(now.minusDays(Math.max(1, properties.getAuditRetentionDays())));
        result.put("loginDeleted", loginDeleted);
        result.put("accessDeleted", accessDeleted);
        result.put("auditDeleted", auditDeleted);
        result.put("totalDeleted", loginDeleted + accessDeleted + auditDeleted);
        log.info(
                "log archive cleanup finished, loginDeleted={}, accessDeleted={}, auditDeleted={}, totalDeleted={}",
                loginDeleted,
                accessDeleted,
                auditDeleted,
                loginDeleted + accessDeleted + auditDeleted);
        return result;
    }

    private long cleanupLoginLogs(LocalDateTime cutoffTime) {
        return sysLoginLogMapper.delete(new LambdaQueryWrapper<SysLoginLog>().lt(SysLoginLog::getLoginTime, cutoffTime));
    }

    private long cleanupAccessLogs(LocalDateTime cutoffTime) {
        return sysApiAccessLogMapper.delete(new LambdaQueryWrapper<SysApiAccessLog>().lt(SysApiAccessLog::getAccessTime, cutoffTime));
    }

    private long cleanupAuditLogs(LocalDateTime cutoffTime) {
        return sysAuditLogMapper.delete(new LambdaQueryWrapper<SysAuditLog>().lt(SysAuditLog::getOpTime, cutoffTime));
    }
}
