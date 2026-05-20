package org.nmgyj.authentication.web.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nmgyj.authentication.config.AccessLogProperties;
import org.nmgyj.authentication.service.ApiAccessLogPersistService;
import org.nmgyj.authentication.web.filter.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 接口访问日志拦截器：统一记录入口行为并支持按路径策略可选落库。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger accessLogger = LoggerFactory.getLogger("access.logger");
    private static final String REQ_START_NS_ATTR = "_reqStartNs";
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(
            Arrays.asList("password", "token", "authorization", "secret", "idcard", "mobile"));

    private final AccessLogProperties properties;
    private final ApiAccessLogPersistService persistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessLogInterceptor(AccessLogProperties properties, ApiAccessLogPersistService persistService) {
        this.properties = properties;
        this.persistService = persistService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        request.setAttribute(REQ_START_NS_ATTR, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) {
        if (!properties.isEnabled()) {
            return;
        }
        long costMs = calcCostMs(request);
        String traceId = resolveTraceId(request);
        String method = request.getMethod();
        String requestUri = request.getRequestURI();
        String uri = requestUri == null ? "" : requestUri;
        String clientIp = resolveClientIp(request);
        String query = resolveQueryString(request);
        int status = response.getStatus();
        Long userId = resolveUserId();
        String username = userId == null ? "" : String.valueOf(userId);

        accessLogger.info(
                "traceId={} userId={} username={} ip={} method={} uri={} query={} status={} costMs={}",
                traceId,
                userId == null ? "" : userId,
                username,
                clientIp,
                method,
                uri,
                query,
                status,
                costMs);

        if (shouldPersistToDb(uri)) {
            persistService.persist(traceId, userId, username, clientIp, uri, method, query, status, costMs);
        }
    }

    private long calcCostMs(HttpServletRequest request) {
        Object value = request.getAttribute(REQ_START_NS_ATTR);
        if (value instanceof Long startNs) {
            return (System.nanoTime() - startNs) / 1_000_000L;
        }
        return -1L;
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        Object attr = request.getAttribute(TraceIdFilter.TRACE_ID_REQUEST_ATTR);
        if (attr instanceof String str && StringUtils.hasText(str)) {
            return str;
        }
        return "";
    }

    private Long resolveUserId() {
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                return Long.valueOf(String.valueOf(loginId));
            }
        } catch (Exception ignored) {
            // 用户上下文解析失败不影响访问日志
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "";
    }

    private String resolveQueryString(HttpServletRequest request) {
        if (!properties.isIncludeQueryString()) {
            return "";
        }
        String query = request.getQueryString();
        if (!StringUtils.hasText(query)) {
            return "";
        }
        String masked = maskQueryString(query);
        int maxLength = Math.max(0, properties.getMaxQueryLength());
        if (maxLength > 0 && masked.length() > maxLength) {
            return masked.substring(0, maxLength) + "...(truncated)";
        }
        return masked;
    }

    private String maskQueryString(String rawQuery) {
        String[] segments = rawQuery.split("&");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            int idx = segment.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = segment.substring(0, idx);
            String lowerKey = key.toLowerCase(Locale.ROOT);
            if (SENSITIVE_KEYS.contains(lowerKey)) {
                segments[i] = key + "=******";
            }
        }
        return String.join("&", segments);
    }

    private boolean shouldPersistToDb(@NonNull String uri) {
        if (!properties.isDbEnabled()) {
            return false;
        }
        List<String> includes = properties.getDbIncludePaths();
        if (includes == null || includes.isEmpty()) {
            return false;
        }
        for (String pattern : includes) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            String normalizedPattern = pattern.trim();
            if (pathMatcher.match(Objects.requireNonNull(normalizedPattern), Objects.requireNonNull(uri))) {
                return true;
            }
        }
        return false;
    }
}
