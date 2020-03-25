package com.abigtomato.shop.order.config;

import com.abigtomato.shop.order.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器
 */
@Configuration
public class ShopWebMvcConfig implements WebMvcConfigurer {

    private LoginInterceptor loginInterceptor;

    @Autowired
    public ShopWebMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加自定义的登录拦截器
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}
