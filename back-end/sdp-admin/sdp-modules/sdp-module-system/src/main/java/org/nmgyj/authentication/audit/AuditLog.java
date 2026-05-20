package org.nmgyj.authentication.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录业务审计日志的方法。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作动作，如 CREATE/UPDATE/DELETE。
     *
     * @return 动作描述
     */
    String action();

    /**
     * 业务类型，如 user/role/order。
     *
     * @return 业务类型
     */
    String bizType();

    /**
     * 业务主键 SpEL，例如 {@code #id} 或 {@code #patch.id}。
     *
     * @return SpEL 表达式
     */
    String bizIdSpEL() default "";

    /**
     * 是否记录操作前后快照。
     *
     * @return true 表示记录 before/after
     */
    boolean recordDiff() default false;
}
