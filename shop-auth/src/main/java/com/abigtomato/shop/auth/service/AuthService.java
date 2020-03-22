package com.abigtomato.shop.auth.service;

import com.abigtomato.shop.model.ums.ext.AuthToken;

import java.util.Optional;

public interface AuthService {

    /**
     * 登录（获取认证信息）
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
}
