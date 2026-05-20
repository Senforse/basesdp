package org.nmgyj.authentication.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import org.nmgyj.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将 Sa-Token 与业务非法参数异常转换为统一 JSON 响应。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理未登录或令牌失效。
     *
     * @param ex Sa-Token 未登录异常
     * @return HTTP 语义由网关或前端结合 {@code code} 判断；此处 {@code code=401}
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException ex, HttpServletRequest request) {
        log.warn(
                "traceId={} exceptionClass={} exceptionMessage={} requestUri={} method={} resultCode={} stackSnippet={}",
                traceId(),
                ex.getClass().getSimpleName(),
                value(ex.getMessage()),
                requestUri(request),
                method(request),
                401,
                stackSnippet(ex));
        return ApiResponse.fail(401, "未登录或登录已过期");
    }

    /**
     * 处理缺少指定权限。
     *
     * @param ex Sa-Token 无权限异常
     * @return 失败响应，{@code code=403}
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermission(NotPermissionException ex, HttpServletRequest request) {
        log.warn(
                "traceId={} exceptionClass={} exceptionMessage={} requestUri={} method={} resultCode={} stackSnippet={}",
                traceId(),
                ex.getClass().getSimpleName(),
                value(ex.getMessage()),
                requestUri(request),
                method(request),
                403,
                stackSnippet(ex));
        return ApiResponse.fail(403, "无权限访问");
    }

    /**
     * 处理缺少指定角色。
     *
     * @param ex Sa-Token 无角色异常
     * @return 失败响应，{@code code=403}
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<Void> handleNotRole(NotRoleException ex, HttpServletRequest request) {
        log.warn(
                "traceId={} exceptionClass={} exceptionMessage={} requestUri={} method={} resultCode={} stackSnippet={}",
                traceId(),
                ex.getClass().getSimpleName(),
                value(ex.getMessage()),
                requestUri(request),
                method(request),
                403,
                stackSnippet(ex));
        return ApiResponse.fail(403, "无此角色权限");
    }

    /**
     * 处理通用非法参数（未被控制器单独捕获时）。
     *
     * @param ex 非法参数异常
     * @return 失败响应，默认错误码 1
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn(
                "traceId={} exceptionClass={} exceptionMessage={} requestUri={} method={} resultCode={} stackSnippet={}",
                traceId(),
                ex.getClass().getSimpleName(),
                value(ex.getMessage()),
                requestUri(request),
                method(request),
                1,
                stackSnippet(ex));
        return ApiResponse.fail(ex.getMessage());
    }

    /**
     * 兜底处理未预期异常，避免漏记错误日志。
     *
     * @param ex 未捕获异常
     * @return 失败响应，错误码 500
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error(
                "traceId={} exceptionClass={} exceptionMessage={} requestUri={} method={} resultCode={} stackSnippet={}",
                traceId(),
                ex.getClass().getSimpleName(),
                value(ex.getMessage()),
                requestUri(request),
                method(request),
                500,
                stackSnippet(ex),
                ex);
        return ApiResponse.fail(500, "系统繁忙，请稍后重试");
    }

    private String traceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "" : traceId;
    }

    private String requestUri(HttpServletRequest request) {
        return request == null || request.getRequestURI() == null ? "" : request.getRequestURI();
    }

    private String method(HttpServletRequest request) {
        return request == null || request.getMethod() == null ? "" : request.getMethod();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String stackSnippet(Throwable throwable) {
        if (throwable == null || throwable.getStackTrace() == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] stack = throwable.getStackTrace();
        int limit = Math.min(stack.length, 5);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(stack[i]);
        }
        return builder.toString();
    }
}
