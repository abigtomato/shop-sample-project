package com.abigtomato.shop.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * cors跨域配置文件
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:1000");    // 指定地址运行跨域
        corsConfiguration.addAllowedHeader("*");                        // 运行任何头信息跨域访问
        corsConfiguration.addAllowedMethod("*");                        // 允许所有的请求方法跨域
        corsConfiguration.setAllowCredentials(true);                    // 允许携带cookie

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);    // 过滤器拦截所有请求

        // cors过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}
