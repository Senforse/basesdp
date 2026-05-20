package org.nmgyj.authentication.service;

import org.nmgyj.authentication.entity.SysAuditLog;
import org.nmgyj.authentication.mapper.SysAuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 审计日志落库服务，失败时降级写日志，不影响主流程。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Service
public class AuditLogPersistService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogPersistService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("audit.logger");

    private final SysAuditLogMapper sysAuditLogMapper;

    public AuditLogPersistService(SysAuditLogMapper sysAuditLogMapper) {
        this.sysAuditLogMapper = sysAuditLogMapper;
    }

    public void persist(SysAuditLog row) {
        if (row == null) {
            return;
        }
        auditLogger.info(
                "traceId={} userId={} username={} bizType={} bizId={} action={} resultCode={} success={} costMs={}",
                value(row.getTraceId()),
                row.getUserId() == null ? "" : row.getUserId(),
                value(row.getUsername()),
                value(row.getBizType()),
                value(row.getBizId()),
                value(row.getAction()),
                row.getResultCode() == null ? "" : row.getResultCode(),
                row.getSuccessFlag() == null ? "" : row.getSuccessFlag(),
                row.getCostMs() == null ? "" : row.getCostMs());
        try {
            sysAuditLogMapper.insert(row);
        } catch (Exception ex) {
            log.warn(
                    "persist audit log failed, traceId={}, bizType={}, bizId={}, action={}",
                    row.getTraceId(),
                    row.getBizType(),
                    row.getBizId(),
                    row.getAction());
        }
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
