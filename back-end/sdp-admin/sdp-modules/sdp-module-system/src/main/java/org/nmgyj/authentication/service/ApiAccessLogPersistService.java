package org.nmgyj.authentication.service;

import org.nmgyj.authentication.entity.SysApiAccessLog;
import org.nmgyj.authentication.mapper.SysApiAccessLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 访问日志落库服务，失败时降级为告警日志，不影响主流程。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Service
public class ApiAccessLogPersistService {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessLogPersistService.class);

    private final SysApiAccessLogMapper sysApiAccessLogMapper;

    public ApiAccessLogPersistService(SysApiAccessLogMapper sysApiAccessLogMapper) {
        this.sysApiAccessLogMapper = sysApiAccessLogMapper;
    }

    public void persist(
            String traceId,
            Long userId,
            String username,
            String clientIp,
            String requestUri,
            String method,
            String queryString,
            int statusCode,
            long costMs) {
        SysApiAccessLog row = new SysApiAccessLog();
        row.setTraceId(traceId);
        row.setUserId(userId);
        row.setUsername(username);
        row.setClientIp(clientIp);
        row.setRequestUri(requestUri);
        row.setHttpMethod(method);
        row.setQueryString(queryString);
        row.setStatusCode(statusCode);
        row.setCostMs(costMs);
        row.setAccessTime(LocalDateTime.now());
        try {
            sysApiAccessLogMapper.insert(row);
        } catch (Exception ex) {
            log.warn("persist access log failed, uri={}, traceId={}", requestUri, traceId);
        }
    }
}
