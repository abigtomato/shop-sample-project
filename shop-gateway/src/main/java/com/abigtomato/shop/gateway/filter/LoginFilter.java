package com.abigtomato.shop.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.gateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoginFilter implements GlobalFilter, Ordered {

    private AuthService authService;

    @Autowired
    public LoginFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String token = this.authService.getTokenFromCookie(request);
        if (StrUtil.isEmpty(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String jwt = this.authService.getJwtFromHeader(request);
        if (StrUtil.isEmpty(jwt)) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }

        long expire = this.authService.getExpire(token);
        if (expire < 0) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);  // 传递到过滤链上，让链上的其他过滤器处理
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
