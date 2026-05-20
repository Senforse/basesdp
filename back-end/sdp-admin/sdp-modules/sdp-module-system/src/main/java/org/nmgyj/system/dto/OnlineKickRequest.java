package org.nmgyj.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 按令牌下线指定会话。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "下线在线用户请求")
public class OnlineKickRequest {

    @Schema(description = "目标会话 token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tokenValue;

    /**
     * @return token
     */
    public String getTokenValue() {
        return tokenValue;
    }

    /**
     * @param tokenValue token
     */
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
