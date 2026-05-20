package org.nmgyj.authentication.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 分页插件与 Mapper 扫描配置。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
@MapperScan(basePackages = "org.nmgyj")
public class MybatisPlusConfig {

    /**
     * @return MyBatis-Plus 拦截器链（含达梦分页）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.DM));
        return interceptor;
    }
}
