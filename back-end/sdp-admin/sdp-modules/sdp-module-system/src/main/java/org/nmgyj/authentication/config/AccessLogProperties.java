package org.nmgyj.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口访问日志配置。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Component
@ConfigurationProperties(prefix = "sdp.logging.access")
public class AccessLogProperties {

    private boolean enabled = true;
    private boolean includeQueryString = true;
    private int maxQueryLength = 2048;
    private boolean dbEnabled = false;
    private List<String> excludePaths = new ArrayList<>(
            List.of("/actuator/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"));
    private List<String> dbIncludePaths = new ArrayList<>(List.of("/api/auth/**"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeQueryString() {
        return includeQueryString;
    }

    public void setIncludeQueryString(boolean includeQueryString) {
        this.includeQueryString = includeQueryString;
    }

    public int getMaxQueryLength() {
        return maxQueryLength;
    }

    public void setMaxQueryLength(int maxQueryLength) {
        this.maxQueryLength = maxQueryLength;
    }

    public boolean isDbEnabled() {
        return dbEnabled;
    }

    public void setDbEnabled(boolean dbEnabled) {
        this.dbEnabled = dbEnabled;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    public List<String> getDbIncludePaths() {
        return dbIncludePaths;
    }

    public void setDbIncludePaths(List<String> dbIncludePaths) {
        this.dbIncludePaths = dbIncludePaths;
    }
}
