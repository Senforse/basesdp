package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 在线会话一行展示数据。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "在线用户会话")
public class OnlineUserVO {

    @Schema(description = "会话令牌（下线操作时回传）")
    private String tokenValue;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "登录名/工号")
    private String username;

    @Schema(description = "用户类型（取首个角色名称，无则为空）")
    private String userType;

    @Schema(description = "应用")
    private String application;

    @Schema(description = "授权类型")
    private String authType;

    @Schema(description = "到期时间（yyyy-MM-dd HH:mm:ss），永不过期则为空")
    private String expireTime;

    /**
     * @return 令牌值
     */
    public String getTokenValue() {
        return tokenValue;
    }

    /**
     * @param tokenValue 令牌值
     */
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
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
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * @return 应用
     */
    public String getApplication() {
        return application;
    }

    /**
     * @param application 应用
     */
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * @return 授权类型
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * @param authType 授权类型
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * @return 到期时间字符串
     */
    public String getExpireTime() {
        return expireTime;
    }

    /**
     * @param expireTime 到期时间字符串
     */
    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
}
