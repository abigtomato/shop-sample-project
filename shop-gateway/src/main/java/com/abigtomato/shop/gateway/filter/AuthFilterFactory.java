package com.abigtomato.shop.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * gateway过滤器工厂
 */
@Component
public class AuthFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private AuthFilter authFilter;

    @Autowired
    public AuthFilterFactory(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Override
    public GatewayFilter apply(Object config) {
        // 自定义过滤器
        return authFilter;
    }
}
