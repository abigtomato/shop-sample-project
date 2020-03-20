package com.abigtomato.shop.auth.service.impl;

import com.abigtomato.shop.auth.service.AuthService;
import com.abigtomato.shop.core.client.ServiceNameList;
import com.abigtomato.shop.core.exception.ExceptionCast;
import com.abigtomato.shop.model.ums.ext.AuthToken;
import com.abigtomato.shop.model.ums.response.AuthCode;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value(value = "${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    private LoadBalancerClient loadBalancerClient;

    private StringRedisTemplate stringRedisTemplate;

    private RestTemplate restTemplate;

    @Autowired
    public AuthServiceImpl(LoadBalancerClient loadBalancerClient,
                           StringRedisTemplate stringRedisTemplate,
                           RestTemplate restTemplate) {
        this.loadBalancerClient = loadBalancerClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<AuthToken> login(String username, String password, String clientId, String clientSecret) {
        Optional<AuthToken> authTokenOptional = this.applyToken(username, password, clientId, clientSecret);
        if (!authTokenOptional.isPresent()) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        String accessToken = authTokenOptional.get().getAccess_token();
        String jsonStr = JSON.toJSONString(accessToken);

        boolean result = this.saveToken(accessToken, jsonStr, tokenValiditySeconds);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authTokenOptional;
    }

    private boolean saveToken(String accessToken, String jsonStr, int tokenValiditySeconds) {
        String key = "user_token:" + accessToken;

        this.stringRedisTemplate.boundValueOps(key).set(jsonStr, tokenValiditySeconds, TimeUnit.SECONDS);

        Long expire = this.stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null && expire > 0;
    }

    private Optional<AuthToken> applyToken(String username, String password, String clientId, String clientSecret) {
        ServiceInstance serviceInstance = this.loadBalancerClient.choose(ServiceNameList.UMS_SERVICE);
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token";

        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = this.getHttpBasic(clientId, clientSecret);
        header.add("Authorization", httpBasic);

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = this.restTemplate.exchange(authUrl,
                HttpMethod.POST, new HttpEntity<>(body, header), Map.class);
        Map bodyMap = exchange.getBody();
        if (bodyMap == null ||
                bodyMap.get("access_token") == null ||
                bodyMap.get("refresh_token") == null ||
                bodyMap.get("jti") == null) {
            if (bodyMap != null && bodyMap.get("error_description") != null) {
                String error_description = (String) bodyMap.get("error_description");
                if (error_description.contains("UserDetailsService returned null")) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                } else if(error_description.contains("坏的凭证")) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return Optional.empty();
        }

        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));             // 用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));  // 刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));       // jwt令牌
        return Optional.of(authToken);
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    @Override
    public void delToken(String uid) {
        String key = "user_token:" + uid;
        this.restTemplate.delete(key);
    }

    @Override
    public Optional<AuthToken> getUserToken(String uid) {
        String key = "user_token:" + uid;
        String value = this.stringRedisTemplate.opsForValue().get(key);

        AuthToken authToken = JSON.parseObject(value, AuthToken.class);
        return Optional.ofNullable(authToken);
    }
}
