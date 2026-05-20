package org.nmgyj.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 提供 BCrypt 密码编码器 Bean。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * @return Spring Security {@link PasswordEncoder} 实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
