package com.abigtomato.shop.order.config;

import com.abigtomato.shop.core.interceptor.FeignClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置feign的拦截器
 */
@Configuration
public class FeignInterceptorConfig {

    @Bean
    public FeignClientInterceptor getFeignClientInterceptor() {
        // 使用自定义feign拦截器，服务间调用传递令牌信息
        return new FeignClientInterceptor();
    }
}
