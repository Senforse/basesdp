package org.nmgyj.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 业务审计日志，对应表 {@code sys_audit_log}。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("TRACE_ID")
    private String traceId;

    @TableField("USER_ID")
    private Long userId;

    @TableField("USERNAME")
    private String username;

    @TableField("CLIENT_IP")
    private String clientIp;

    @TableField("REQUEST_URI")
    private String requestUri;

    @TableField("HTTP_METHOD")
    private String httpMethod;

    @TableField("BIZ_TYPE")
    private String bizType;

    @TableField("BIZ_ID")
    private String bizId;

    @TableField("ACTION")
    private String action;

    @TableField("BEFORE_JSON")
    private String beforeJson;

    @TableField("AFTER_JSON")
    private String afterJson;

    @TableField("RESULT_CODE")
    private Integer resultCode;

    @TableField("SUCCESS_FLAG")
    private Integer successFlag;

    @TableField("COST_MS")
    private Long costMs;

    @TableField("ERROR_MESSAGE")
    private String errorMessage;

    @TableField("OP_TIME")
    private LocalDateTime opTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson) {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public void setAfterJson(String afterJson) {
        this.afterJson = afterJson;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public Integer getSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(Integer successFlag) {
        this.successFlag = successFlag;
    }

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getOpTime() {
        return opTime;
    }

    public void setOpTime(LocalDateTime opTime) {
        this.opTime = opTime;
    }
}
