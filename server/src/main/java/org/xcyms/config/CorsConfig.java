package org.xcyms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * <p>
 *     跨域配置
 * </p>
 * @author liu-xu
 * @date 2026年01月11日 16:08
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. 是否允许发送 Cookie
        config.setAllowCredentials(true);

        // 2. 设置允许跨域请求的域名（Spring Boot 3 必须使用 addAllowedOriginPattern）
        // 这里可以写 "*" 表示允许所有，或者写具体的域名如 "http://localhost:5173"
        config.addAllowedOriginPattern("*");

        // 3. 设置允许的 Header
        config.addAllowedHeader("*");

        // 4. 设置允许的 HTTP 方法
        config.addAllowedMethod("*");

        // 5. 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 6. 对所有接口生效
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
