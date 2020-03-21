package com.abigtomato.shop.gateway.service;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.core.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private StringRedisTemplate redisTemplate;

    @Autowired
    public AuthService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getTokenFromCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        HttpCookie cookie = cookies.getFirst("uid");
        if (cookie == null) {
            return null;
        }

        String accessToken = cookie.getValue();
        if (StrUtil.isEmpty(accessToken)) {
            return null;
        }

        return accessToken;
    }

    public String getJwtFromHeader(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        String authorization = headers.getFirst("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return null;
        }
        if (!StrUtil.startWith(authorization, "Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }

    public long getExpire(String token) {
        String key = "user_token:" + token;
        Long expire = this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expire == null) {
            return -1;
        }
        return expire;
    }
}
