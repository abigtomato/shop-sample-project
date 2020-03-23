package com.abigtomato.shop.auth.service;

import com.abigtomato.shop.model.ums.ext.AuthToken;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Optional;

public interface AuthService {

    /**
     * 登录（获取oauth2认证信息）
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    Optional<AuthToken> login(String username, String password, String clientId, String clientSecret);

    /**
     * 删除令牌信息
     * @param uid
     */
    void delToken(String uid);

    /**
     * 根据身份令牌获取完整令牌信息
     * @param uid
     * @return
     */
    Optional<AuthToken> getUserToken(String uid);

    /**
     * 从cookie中获取身份令牌
     * @param request
     * @return
     */
    String getTokenFromCookie(ServerHttpRequest request);

    /**
     * 从header中获取jwt令牌
     * @param request
     * @return
     */
    String getJwtFromHeader(ServerHttpRequest request);

    /**
     * 获取redis中令牌信息的过期时间
     * @param token
     * @return
     */
    long getExpire(String token);
}
