package com.abigtomato.shop.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.gateway.client.AuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 用户身份认证过滤器
 */
@Component
public class AuthFilter implements GatewayFilter {

    private AuthClient authClient;

    @Autowired
    public AuthFilter(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String token = this.authClient.getTokenFromCookie(request);
        if (StrUtil.isEmpty(token)) {
            // cookie中没有携带身份令牌，拦截
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String jwt = this.authClient.getJwtFromHeader(request);
        if (StrUtil.isEmpty(jwt)) {
            // header中没有jwt令牌，拦截
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }

        long expire = this.authClient.getExpire(token);
        if (expire < 0) {
            // redis中的令牌信息过期，拦截
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }

        // 传递到过滤链上，让链上的其他过滤器处理（放行）
        return chain.filter(exchange);
    }
}
