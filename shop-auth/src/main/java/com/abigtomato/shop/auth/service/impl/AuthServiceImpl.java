package com.abigtomato.shop.auth.service.impl;

import cn.hutool.core.util.StrUtil;
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
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

    private static final String REDIS_KEY_PREFIX = "user_token:";

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
        // 获取身份令牌
        Optional<AuthToken> authTokenOptional = this.applyToken(username, password, clientId, clientSecret);
        if (!authTokenOptional.isPresent()) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        // 保存令牌信息到redis
        String accessToken = authTokenOptional.get().getAccess_token();
        String jsonStr = JSON.toJSONString(accessToken);
        boolean result = this.saveTokenToRedis(accessToken, jsonStr, tokenValiditySeconds);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authTokenOptional;
    }

    /**
     * 保存token到redis
     * @param accessToken
     * @param jsonStr
     * @param tokenValiditySeconds
     * @return
     */
    private boolean saveTokenToRedis(String accessToken, String jsonStr, int tokenValiditySeconds) {
        String key = REDIS_KEY_PREFIX + accessToken;

        this.stringRedisTemplate.boundValueOps(key).set(jsonStr, tokenValiditySeconds, TimeUnit.SECONDS);

        Long expire = this.stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null && expire > 0;
    }

    /**
     * 获取oauth2的认证信息
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    private Optional<AuthToken> applyToken(String username, String password, String clientId, String clientSecret) {
        // 获取oauth2认证接口的地址
        ServiceInstance serviceInstance = this.loadBalancerClient.choose(ServiceNameList.UMS_SERVICE);
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token";

        // 构造header
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = this.getHttpBasic(clientId, clientSecret);
        header.add("Authorization", httpBasic); // http basic规范传递oauth2的客户端信息

        // 构造body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password"); // 密码认证方式
        body.add("username", username);
        body.add("password", password);

        // 设置400和401错误不由框架控制
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        // 发送请求获取响应
        ResponseEntity<Map> exchange = this.restTemplate.exchange(authUrl,
                HttpMethod.POST, new HttpEntity<>(body, header), Map.class);
        Map bodyMap = exchange.getBody();

        // 判断错误情况，自定义异常响应
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

        // 封装token对象
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));             // 用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));  // 刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));       // jwt令牌
        return Optional.of(authToken);
    }

    /**
     * 获取http basic串
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHttpBasic(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    @Override
    public void delToken(String uid) {
        String key = REDIS_KEY_PREFIX + uid;
        this.restTemplate.delete(key);
    }

    @Override
    public Optional<AuthToken> getUserToken(String uid) {
        String key = REDIS_KEY_PREFIX + uid;
        String value = this.stringRedisTemplate.opsForValue().get(key);

        AuthToken authToken = JSON.parseObject(value, AuthToken.class);
        return Optional.ofNullable(authToken);
    }

    @Override
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

    @Override
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

    @Override
    public long getExpire(String token) {
        String key = REDIS_KEY_PREFIX + token;
        Long expire = this.stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expire == null) {
            return -1;
        }
        return expire;
    }
}
