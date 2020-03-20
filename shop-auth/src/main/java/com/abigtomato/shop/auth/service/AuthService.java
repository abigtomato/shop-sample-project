package com.abigtomato.shop.auth.service;

import com.abigtomato.shop.model.ums.ext.AuthToken;

import java.util.Optional;

public interface AuthService {

    Optional<AuthToken> login(String username, String password, String clientId, String clientSecret);

    void delToken(String uid);

    Optional<AuthToken> getUserToken(String uid);
}
