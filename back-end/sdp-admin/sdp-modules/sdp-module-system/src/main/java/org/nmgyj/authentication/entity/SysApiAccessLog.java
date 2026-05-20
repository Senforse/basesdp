package org.nmgyj.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 接口访问日志，对应表 {@code sys_api_access_log}。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@TableName("sys_api_access_log")
public class SysApiAccessLog {

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

    @TableField("QUERY_STRING")
    private String queryString;

    @TableField("STATUS_CODE")
    private Integer statusCode;

    @TableField("COST_MS")
    private Long costMs;

    @TableField("ACCESS_TIME")
    private LocalDateTime accessTime;

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

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }
}
