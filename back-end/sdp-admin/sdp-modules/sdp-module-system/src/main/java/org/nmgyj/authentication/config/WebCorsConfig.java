package org.nmgyj.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 规则（含暴露 {@code satoken} 响应头）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("satoken")
                .maxAge(3600);
    }
}
