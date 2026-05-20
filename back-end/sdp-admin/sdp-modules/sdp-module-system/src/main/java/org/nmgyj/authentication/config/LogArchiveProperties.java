package org.nmgyj.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 日志归档与清理任务配置。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Component
@ConfigurationProperties(prefix = "sdp.logging.archive")
public class LogArchiveProperties {

    private boolean enabled = true;
    private String cron = "0 30 3 * * ?";
    private int loginRetentionDays = 90;
    private int accessRetentionDays = 30;
    private int auditRetentionDays = 180;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getLoginRetentionDays() {
        return loginRetentionDays;
    }

    public void setLoginRetentionDays(int loginRetentionDays) {
        this.loginRetentionDays = loginRetentionDays;
    }

    public int getAccessRetentionDays() {
        return accessRetentionDays;
    }

    public void setAccessRetentionDays(int accessRetentionDays) {
        this.accessRetentionDays = accessRetentionDays;
    }

    public int getAuditRetentionDays() {
        return auditRetentionDays;
    }

    public void setAuditRetentionDays(int auditRetentionDays) {
        this.auditRetentionDays = auditRetentionDays;
    }
}
