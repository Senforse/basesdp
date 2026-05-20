package org.nmgyj.authentication.audit;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.nmgyj.authentication.entity.SysAuditLog;
import org.nmgyj.authentication.service.AuditLogPersistService;
import org.nmgyj.authentication.web.filter.TraceIdFilter;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.mapper.SysUserMapper;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 审计注解切面：采集操作上下文并异步风格落库。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final int MAX_JSON_LENGTH = 4000;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(
            Arrays.asList("password", "token", "authorization", "secret", "idcard", "mobile"));

    private final ObjectMapper objectMapper;
    private final AuditLogPersistService auditLogPersistService;
    private final SysUserMapper sysUserMapper;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditLogAspect(
            ObjectMapper objectMapper,
            AuditLogPersistService auditLogPersistService,
            SysUserMapper sysUserMapper) {
        this.objectMapper = objectMapper;
        this.auditLogPersistService = auditLogPersistService;
        this.sysUserMapper = sysUserMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startNs = System.nanoTime();
        Method method = resolveMethod(Objects.requireNonNull(joinPoint));
        Object[] args = joinPoint.getArgs();
        String beforeJson = auditLog.recordDiff()
                ? toJsonSafely(maskSensitive(buildArgSnapshot(Objects.requireNonNull(method), args)))
                : "";

        Object result = null;
        Throwable thrown = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            thrown = ex;
            throw ex;
        } finally {
            long costMs = (System.nanoTime() - startNs) / 1_000_000L;
            persistAuditLog(auditLog, method, args, result, thrown, beforeJson, costMs);
        }
    }

    private void persistAuditLog(
            AuditLog annotation,
            Method method,
            Object[] args,
            Object result,
            Throwable throwable,
            String beforeJson,
            long costMs) {
        SysAuditLog row = new SysAuditLog();
        row.setTraceId(resolveTraceId());
        Long userId = resolveUserId();
        row.setUserId(userId);
        row.setUsername(resolveUsername(userId));
        row.setClientIp(resolveClientIp());
        row.setRequestUri(resolveRequestUri());
        row.setHttpMethod(resolveHttpMethod());
        row.setBizType(annotation.bizType());
        row.setBizId(evaluateBizId(annotation.bizIdSpEL(), Objects.requireNonNull(method), args, result));
        row.setAction(annotation.action());
        row.setBeforeJson(beforeJson);
        if (annotation.recordDiff()) {
            row.setAfterJson(toJsonSafely(maskSensitive(result)));
        } else {
            row.setAfterJson("");
        }
        row.setCostMs(costMs);
        row.setOpTime(LocalDateTime.now());
        if (throwable == null) {
            row.setSuccessFlag(1);
            row.setResultCode(resolveResultCode(result));
            row.setErrorMessage("");
        } else {
            row.setSuccessFlag(0);
            row.setResultCode(1);
            row.setErrorMessage(truncate(throwable.getClass().getSimpleName() + ": " + throwable.getMessage(), MAX_ERROR_MESSAGE_LENGTH));
        }
        auditLogPersistService.persist(row);
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget() == null ? method.getDeclaringClass() : joinPoint.getTarget().getClass();
        return AopUtils.getMostSpecificMethod(Objects.requireNonNull(method), targetClass);
    }

    private Map<String, Object> buildArgSnapshot(Method method, Object[] args) {
        Map<String, Object> payload = new LinkedHashMap<>();
        String[] names = parameterNameDiscoverer.getParameterNames(Objects.requireNonNull(method));
        if (args == null || args.length == 0) {
            return payload;
        }
        for (int i = 0; i < args.length; i++) {
            String name = names != null && i < names.length ? names[i] : "arg" + i;
            payload.put(name, args[i]);
        }
        return payload;
    }

    private String evaluateBizId(String bizIdSpel, Method method, Object[] args, Object result) {
        if (!StringUtils.hasText(bizIdSpel)) {
            return "";
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("result", result);
            String[] names = parameterNameDiscoverer.getParameterNames(Objects.requireNonNull(method));
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (names != null && i < names.length) {
                        context.setVariable(names[i], args[i]);
                    }
                    context.setVariable("p" + i, args[i]);
                    context.setVariable("a" + i, args[i]);
                }
            }
            String expression = bizIdSpel == null ? "" : bizIdSpel;
            Object value = spelParser.parseExpression(expression).getValue(context);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception ex) {
            return "";
        }
    }

    private int resolveResultCode(Object result) {
        if (result instanceof ApiResponse<?> response && response.getCode() != null) {
            return response.getCode();
        }
        return 0;
    }

    private Long resolveUserId() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return Long.valueOf(String.valueOf(StpUtil.getLoginId()));
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveUsername(Long userId) {
        if (userId == null) {
            return "";
        }
        try {
            SysUser user = sysUserMapper.selectById(userId);
            return user == null || !StringUtils.hasText(user.getUsername()) ? String.valueOf(userId) : user.getUsername();
        } catch (Exception ex) {
            return String.valueOf(userId);
        }
    }

    private String resolveTraceId() {
        String traceId = org.slf4j.MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        return traceId == null ? "" : traceId;
    }

    private String resolveClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return "";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "" : request.getRemoteAddr();
    }

    private String resolveRequestUri() {
        HttpServletRequest request = currentRequest();
        return request == null || request.getRequestURI() == null ? "" : request.getRequestURI();
    }

    private String resolveHttpMethod() {
        HttpServletRequest request = currentRequest();
        return request == null || request.getMethod() == null ? "" : request.getMethod();
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        return attrs.getRequest();
    }

    private Object maskSensitive(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
                    masked.put(key, "******");
                } else {
                    masked.put(key, maskSensitive(entry.getValue()));
                }
            }
            return masked;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::maskSensitive).toList();
        }
        return value;
    }

    private String toJsonSafely(Object value) {
        if (value == null) {
            return "";
        }
        try {
            return truncate(objectMapper.writeValueAsString(value), MAX_JSON_LENGTH);
        } catch (JsonProcessingException ex) {
            return truncate(String.valueOf(value), MAX_JSON_LENGTH);
        }
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }
}
