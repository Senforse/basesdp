package org.nmgyj.authentication.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 Sa-Token 登录校验拦截：默认校验 {@code /api/**}，放行登录与 OpenAPI 文档路径。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    /**
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new SaInterceptor(
                                handle ->
                                        SaRouter.match("/api/**")
                                                .notMatch("/api/auth/login")
                                                .notMatch("/error")
                                                .notMatch("/actuator/**")
                                                .notMatch("/v3/api-docs/**")
                                                .notMatch("/swagger-ui/**")
                                                .notMatch("/swagger-ui.html")
                                                .notMatchMethod("OPTIONS")
                                                .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
