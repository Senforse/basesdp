package org.nmgyj.authentication.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.authentication.entity.SysApiAccessLog;
import org.nmgyj.authentication.entity.SysAuditLog;
import org.nmgyj.authentication.entity.SysLoginLog;
import org.nmgyj.authentication.service.LogArchiveService;
import org.nmgyj.authentication.service.LogQueryService;
import org.nmgyj.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 日志查询与归档运维接口。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/api/admin/logs")
@Tag(name = "日志管理")
public class AdminLogController {

    private final LogQueryService logQueryService;
    private final LogArchiveService logArchiveService;

    public AdminLogController(LogQueryService logQueryService, LogArchiveService logArchiveService) {
        this.logQueryService = logQueryService;
        this.logArchiveService = logArchiveService;
    }

    @Operation(summary = "登录日志分页查询")
    @GetMapping("/login")
    @SaCheckPermission("system:log:list")
    public ApiResponse<Page<SysLoginLog>> loginLogs(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Long pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "loginStatus", required = false) Integer loginStatus,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(logQueryService.pageLoginLogs(pageNo, pageSize, username, loginStatus, startTime, endTime));
    }

    @Operation(summary = "访问日志分页查询")
    @GetMapping("/access")
    @SaCheckPermission("system:log:list")
    public ApiResponse<Page<SysApiAccessLog>> accessLogs(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Long pageSize,
            @RequestParam(value = "traceId", required = false) String traceId,
            @RequestParam(value = "requestUri", required = false) String requestUri,
            @RequestParam(value = "statusCode", required = false) Integer statusCode,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(logQueryService.pageAccessLogs(pageNo, pageSize, traceId, requestUri, statusCode, startTime, endTime));
    }

    @Operation(summary = "审计日志分页查询")
    @GetMapping("/audit")
    @SaCheckPermission("system:log:list")
    public ApiResponse<Page<SysAuditLog>> auditLogs(
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Long pageSize,
            @RequestParam(value = "traceId", required = false) String traceId,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "bizId", required = false) String bizId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "successFlag", required = false) Integer successFlag,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(logQueryService.pageAuditLogs(
                pageNo, pageSize, traceId, bizType, bizId, action, successFlag, startTime, endTime));
    }

    @Operation(summary = "执行一次历史日志归档清理")
    @PostMapping("/archive/cleanup")
    @SaCheckPermission("system:log:archive")
    public ApiResponse<Map<String, Long>> cleanupArchive() {
        return ApiResponse.success(logArchiveService.cleanupExpiredLogs());
    }
}
