package org.nmgyj.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户登录请求体。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "明文或临时密码（生产环境建议配合 HTTPS）", example = "Admin@123")
    private String password;

    /**
     * @return 登录用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username 登录用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 登录密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 登录密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
