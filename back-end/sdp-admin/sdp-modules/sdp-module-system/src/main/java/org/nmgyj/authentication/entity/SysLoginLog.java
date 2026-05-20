package org.nmgyj.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 登录审计日志，对应表 {@code sys_login_log}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "登录日志")
@TableName("sys_login_log")
public class SysLoginLog {

    @Schema(description = "主键", example = "1")
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户主键，匿名失败场景可为空", example = "1")
    @TableField("USER_ID")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    @TableField("USERNAME")
    private String username;

    @Schema(description = "登录 IP", example = "192.168.1.10")
    @TableField("LOGIN_IP")
    private String loginIp;

    @Schema(description = "User-Agent", example = "Mozilla/5.0 ...")
    @TableField("USER_AGENT")
    private String userAgent;

    @Schema(description = "是否成功：1 成功 0 失败", example = "1")
    @TableField("LOGIN_STATUS")
    private Integer loginStatus;

    /**
     * 日志类型：1 登录 2 登出。
     */
    @Schema(description = "日志类型：1 登录 2 登出", example = "1")
    @TableField("LOG_TYPE")
    private Integer logType;

    @Schema(description = "备注说明", example = "登录成功")
    @TableField("REMARK")
    private String remark;

    @Schema(description = "发生时间", example = "2026-05-10T12:00:00")
    @TableField("LOGIN_TIME")
    private LocalDateTime loginTime;

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * @return 登录 IP
     */
    public String getLoginIp() {
        return loginIp;
    }

    /**
     * @param loginIp 登录 IP
     */
    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    /**
     * @return User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent User-Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return 登录是否成功
     */
    public Integer getLoginStatus() {
        return loginStatus;
    }

    /**
     * @param loginStatus 登录是否成功
     */
    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
    }

    /**
     * @return 日志类型
     */
    public Integer getLogType() {
        return logType;
    }

    /**
     * @param logType 日志类型
     */
    public void setLogType(Integer logType) {
        this.logType = logType;
    }

    /**
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return 登录时间
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * @param loginTime 登录时间
     */
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}
