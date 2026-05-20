package org.nmgyj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SDP 管理端 Spring Boot 启动类，负责拉起 Web、持久化与 Sa-Token 等模块。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@SpringBootApplication
@EnableScheduling
public class SdpServerApplication {

    /**
     * 入口方法。
     *
     * @param args JVM 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SdpServerApplication.class, args);
    }
}
