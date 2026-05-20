package org.nmgyj.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.dto.OnlineKickRequest;
import org.nmgyj.system.dto.OnlineUserPageResult;
import org.nmgyj.system.service.OnlineUserAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线会话查询与强制下线。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/admin/online-users")
@Tag(name = "在线用户")
public class OnlineUserAdminController {

    private final OnlineUserAdminService onlineUserAdminService;

    /**
     * @param onlineUserAdminService 在线用户服务
     */
    public OnlineUserAdminController(OnlineUserAdminService onlineUserAdminService) {
        this.onlineUserAdminService = onlineUserAdminService;
    }

    /**
     * 分页查询在线用户会话。
     *
     * @param application 应用编码，可选
     * @param userCode    在线用户编码（用户名），模糊匹配，可选
     * @param page        页码，默认 1
     * @param size        每页条数，默认 10，最大 100
     * @return 分页数据
     */
    @Operation(summary = "在线用户分页列表")
    @GetMapping
    @SaCheckPermission("system:online:list")
    public ApiResponse<OnlineUserPageResult> page(
            @RequestParam(value = "application", required = false) String application,
            @RequestParam(value = "userCode", required = false) String userCode,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        return ApiResponse.success(onlineUserAdminService.page(application, userCode, page, size));
    }

    /**
     * 按令牌下线会话。
     *
     * @param body 请求体（含 tokenValue）
     * @return 成功或失败信息
     */
    @Operation(summary = "下线指定会话")
    @PostMapping("/kick")
    @SaCheckPermission("system:online:kick")
    public ApiResponse<Void> kick(@RequestBody OnlineKickRequest body) {
        try {
            onlineUserAdminService.kickByToken(body.getTokenValue());
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }
}
