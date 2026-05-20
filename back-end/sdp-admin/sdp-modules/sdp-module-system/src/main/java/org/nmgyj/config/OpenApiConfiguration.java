package org.nmgyj.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 Springdoc OpenAPI 文档元数据（标题、描述、版本）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * 构建全局 OpenAPI 描述。
     *
     * @return OpenAPI 根对象，供 Swagger UI 展示
     */
    @Bean
    public OpenAPI sdpAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SDP Admin API")
                        .description("智研 SDP 管理端 REST 接口；鉴权使用 Sa-Token，前端按登录接口返回的 token 请求头名称携带令牌。")
                        .version("1.0.0"));
    }
}
