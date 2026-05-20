package org.nmgyj.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.nmgyj.authentication.entity.SysApiAccessLog;
import org.nmgyj.authentication.entity.SysAuditLog;
import org.nmgyj.authentication.entity.SysLoginLog;
import org.nmgyj.authentication.mapper.SysApiAccessLogMapper;
import org.nmgyj.authentication.mapper.SysAuditLogMapper;
import org.nmgyj.authentication.mapper.SysLoginLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 日志查询服务，统一封装登录/访问/审计日志分页查询条件。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Service
public class LogQueryService {

    private final SysLoginLogMapper sysLoginLogMapper;
    private final SysApiAccessLogMapper sysApiAccessLogMapper;
    private final SysAuditLogMapper sysAuditLogMapper;

    public LogQueryService(
            SysLoginLogMapper sysLoginLogMapper,
            SysApiAccessLogMapper sysApiAccessLogMapper,
            SysAuditLogMapper sysAuditLogMapper) {
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.sysApiAccessLogMapper = sysApiAccessLogMapper;
        this.sysAuditLogMapper = sysAuditLogMapper;
    }

    public Page<SysLoginLog> pageLoginLogs(
            long pageNo,
            long pageSize,
            String username,
            Integer loginStatus,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        Page<SysLoginLog> page = new Page<>(normalizePageNo(pageNo), normalizePageSize(pageSize));
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysLoginLog::getUsername, username.trim());
        }
        if (loginStatus != null) {
            wrapper.eq(SysLoginLog::getLoginStatus, loginStatus);
        }
        if (startTime != null) {
            wrapper.ge(SysLoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysLoginLog::getLoginTime, endTime);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return sysLoginLogMapper.selectPage(page, wrapper);
    }

    public Page<SysApiAccessLog> pageAccessLogs(
            long pageNo,
            long pageSize,
            String traceId,
            String requestUri,
            Integer statusCode,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        Page<SysApiAccessLog> page = new Page<>(normalizePageNo(pageNo), normalizePageSize(pageSize));
        LambdaQueryWrapper<SysApiAccessLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceId)) {
            wrapper.eq(SysApiAccessLog::getTraceId, traceId.trim());
        }
        if (StringUtils.hasText(requestUri)) {
            wrapper.like(SysApiAccessLog::getRequestUri, requestUri.trim());
        }
        if (statusCode != null) {
            wrapper.eq(SysApiAccessLog::getStatusCode, statusCode);
        }
        if (startTime != null) {
            wrapper.ge(SysApiAccessLog::getAccessTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysApiAccessLog::getAccessTime, endTime);
        }
        wrapper.orderByDesc(SysApiAccessLog::getAccessTime);
        return sysApiAccessLogMapper.selectPage(page, wrapper);
    }

    public Page<SysAuditLog> pageAuditLogs(
            long pageNo,
            long pageSize,
            String traceId,
            String bizType,
            String bizId,
            String action,
            Integer successFlag,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        Page<SysAuditLog> page = new Page<>(normalizePageNo(pageNo), normalizePageSize(pageSize));
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceId)) {
            wrapper.eq(SysAuditLog::getTraceId, traceId.trim());
        }
        if (StringUtils.hasText(bizType)) {
            wrapper.eq(SysAuditLog::getBizType, bizType.trim());
        }
        if (StringUtils.hasText(bizId)) {
            wrapper.eq(SysAuditLog::getBizId, bizId.trim());
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(SysAuditLog::getAction, action.trim());
        }
        if (successFlag != null) {
            wrapper.eq(SysAuditLog::getSuccessFlag, successFlag);
        }
        if (startTime != null) {
            wrapper.ge(SysAuditLog::getOpTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysAuditLog::getOpTime, endTime);
        }
        wrapper.orderByDesc(SysAuditLog::getOpTime);
        return sysAuditLogMapper.selectPage(page, wrapper);
    }

    private long normalizePageNo(long pageNo) {
        return Math.max(1L, pageNo);
    }

    private long normalizePageSize(long pageSize) {
        return Math.min(200L, Math.max(1L, pageSize));
    }
}
