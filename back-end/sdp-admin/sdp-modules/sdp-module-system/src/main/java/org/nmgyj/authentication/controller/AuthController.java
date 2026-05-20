package org.nmgyj.authentication.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.nmgyj.authentication.dto.LoginRequest;
import org.nmgyj.authentication.dto.LoginResponse;
import org.nmgyj.authentication.service.AuthService;
import org.nmgyj.common.ApiResponse;
import org.nmgyj.system.dto.MenuRouteVO;
import org.nmgyj.system.entity.SysUser;
import org.nmgyj.system.mapper.SysUserMapper;
import org.nmgyj.system.service.RbacPermissionService;
import org.nmgyj.system.service.SysMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证相关接口：登录、登出、当前用户菜单与个人信息。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证与会话")
public class AuthController {

    private final AuthService authService;
    private final SysMenuService sysMenuService;
    private final SysUserMapper sysUserMapper;
    private final RbacPermissionService rbacPermissionService;

    /**
     * @param authService             登录业务服务
     * @param sysMenuService          菜单构建服务
     * @param sysUserMapper           用户查询 Mapper
     * @param rbacPermissionService   权限与角色查询服务
     */
    public AuthController(
            AuthService authService,
            SysMenuService sysMenuService,
            SysUserMapper sysUserMapper,
            RbacPermissionService rbacPermissionService) {
        this.authService = authService;
        this.sysMenuService = sysMenuService;
        this.sysUserMapper = sysUserMapper;
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * 用户名密码登录；失败时返回 {@link ApiResponse#getCode()} 非 0。
     *
     * @param request         登录凭证
     * @param servletRequest  HTTP 请求（解析客户端 IP、UA）
     * @return 统一响应，成功时 {@code data} 含 token 与用户信息
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        try {
            String loginIp = getClientIp(servletRequest);
            String ua = servletRequest.getHeader("User-Agent");
            return ApiResponse.success(authService.login(request, loginIp, ua));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.fail(ex.getMessage());
        }
    }

    /**
     * 注销当前会话。
     *
     * @return 统一响应
     */
    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.success(null);
    }

    /**
     * 获取当前登录用户可见的前端路由树。
     *
     * @return 路由节点列表
     */
    @Operation(summary = "当前用户菜单路由")
    @GetMapping("/menus")
    public ApiResponse<List<MenuRouteVO>> menus() {
        long uid = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(sysMenuService.buildUserMenuRoutes(uid));
    }

    /**
     * 获取当前登录用户权限码集合（用于按钮/操作级权限控制）。
     *
     * @return 权限码列表
     */
    @Operation(summary = "当前用户权限码")
    @GetMapping("/permissions")
    public ApiResponse<List<String>> permissions() {
        long uid = StpUtil.getLoginIdAsLong();
        return ApiResponse.success(rbacPermissionService.listPermissionCodes(uid));
    }

    /**
     * 获取当前登录用户基本信息（不含密码）。
     *
     * @return 用户信息；用户不存在时返回失败响应
     */
    @Operation(summary = "当前用户资料")
    @GetMapping("/profile")
    public ApiResponse<LoginResponse> profile() {
        long uid = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(uid);
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setRoles(rbacPermissionService.listRoleCodes(uid));
        return ApiResponse.success(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
