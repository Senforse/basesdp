package org.nmgyj.authentication.config;

import org.nmgyj.authentication.web.interceptor.AccessLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Web 日志拦截器注册配置。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Configuration
public class WebMvcLoggingConfig implements WebMvcConfigurer {

    private final AccessLogProperties accessLogProperties;
    private final AccessLogInterceptor accessLogInterceptor;

    public WebMvcLoggingConfig(AccessLogProperties accessLogProperties, AccessLogInterceptor accessLogInterceptor) {
        this.accessLogProperties = accessLogProperties;
        this.accessLogInterceptor = accessLogInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        if (!accessLogProperties.isEnabled()) {
            return;
        }
        List<String> excludes = accessLogProperties.getExcludePaths() == null
                ? Collections.emptyList()
                : accessLogProperties.getExcludePaths();
        InterceptorRegistration registration = registry
                .addInterceptor(Objects.requireNonNull(accessLogInterceptor))
                .addPathPatterns("/**");
        for (String exclude : excludes) {
            if (StringUtils.hasText(exclude)) {
                registration.excludePathPatterns(exclude.trim());
            }
        }
    }
}
