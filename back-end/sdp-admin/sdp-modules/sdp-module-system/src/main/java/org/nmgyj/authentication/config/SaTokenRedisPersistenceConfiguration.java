package org.nmgyj.authentication.config;

import cn.dev33.satoken.dao.SaTokenDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 标明会话与在线用户数据的存储链路：由 Sa-Token 通过 {@link SaTokenDao} 写入 {@code spring.data.redis} 配置的 Redis，
 * 而非业务代码自建 RedisTemplate 写在线用户表。
 * <p>
 * 依赖模块 {@code sdp-common-security}：{@code spring-boot-starter-data-redis} + {@code sa-token-redis-jackson}。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
@ConditionalOnBean(SaTokenDao.class)
public class SaTokenRedisPersistenceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SaTokenRedisPersistenceConfiguration.class);

    private final SaTokenDao saTokenDao;

    /**
     * @param saTokenDao Sa-Token 持久层 Bean（启用 Redis-Jackson 时为 Redis 实现）
     */
    public SaTokenRedisPersistenceConfiguration(SaTokenDao saTokenDao) {
        this.saTokenDao = saTokenDao;
    }

    /**
     * 启动时打印当前 SaTokenDao 实现类，便于确认在线会话是否落在 Redis。
     */
    @PostConstruct
    public void logPersistenceBackend() {
        String name = saTokenDao.getClass().getName();
        boolean redisJackson = name.contains("RedisJackson") || name.toLowerCase().contains("redisjackson");
        if (redisJackson) {
            log.info(
                    "[Sa-Token] SaTokenDao={}（Redis + Jackson）。Token / Session / Token-Session 使用 spring.data.redis；在线用户管理读取同一持久层。",
                    name);
        } else {
            log.warn(
                    "[Sa-Token] SaTokenDao={}，未检测到 Redis-Jackson 典型实现名。若在线用户列表异常，请确认已引入 sa-token-redis-jackson 且 spring.data.redis 已配置。",
                    name);
        }
    }
}
