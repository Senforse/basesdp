package org.nmgyj.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 登录成功或个人信息接口返回的数据结构。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "登录 / 当前用户信息响应")
public class LoginResponse {

    @Schema(description = "Sa-Token 令牌值", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 前端应在请求头携带此名称，值为 token（默认 satoken）。
     */
    @Schema(description = "前端需在请求头携带的令牌名称", example = "satoken")
    private String tokenHeaderName;

    @Schema(description = "用户主键", example = "1")
    private Long userId;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "展示名称", example = "系统管理员")
    private String displayName;

    @Schema(description = "角色编码列表", example = "[\"SUPER_ADMIN\"]")
    private List<String> roles;

    /**
     * @return 令牌值
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token 令牌值
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return 请求头名称
     */
    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    /**
     * @param tokenHeaderName 请求头名称
     */
    public void setTokenHeaderName(String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    /**
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return 登录账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username 登录账号
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName 展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return 角色编码列表
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * @param roles 角色编码列表
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
