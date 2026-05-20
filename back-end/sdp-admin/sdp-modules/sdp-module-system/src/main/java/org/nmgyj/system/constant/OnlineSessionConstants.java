package org.nmgyj.system.constant;

/**
 * 登录后在 Sa-Token Token-Session 中写入的扩展字段键名，供在线用户列表展示。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
public final class OnlineSessionConstants {

    private OnlineSessionConstants() {
    }

    /** 应用标识，当前后台固定为 {@link #DEFAULT_APPLICATION}。 */
    public static final String APPLICATION = "sdpApplication";

    /** 授权方式，如 password。 */
    public static final String AUTH_TYPE = "sdpAuthType";

    /** 默认应用编码（管理端）。 */
    public static final String DEFAULT_APPLICATION = "system";
}
