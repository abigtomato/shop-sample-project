package com.abigtomato.shop.auth.controller;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.auth.AuthApi;
import com.abigtomato.shop.auth.service.AuthService;
import com.abigtomato.shop.core.exception.ExceptionCast;
import com.abigtomato.shop.core.response.CommonCode;
import com.abigtomato.shop.core.response.ResponseResult;
import com.abigtomato.shop.core.utils.CookieUtil;
import com.abigtomato.shop.model.ums.ext.AuthToken;
import com.abigtomato.shop.model.ums.request.LoginRequest;
import com.abigtomato.shop.model.ums.response.AuthCode;
import com.abigtomato.shop.model.ums.response.JwtResult;
import com.abigtomato.shop.model.ums.response.LoginResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping
@Slf4j
public class AuthController implements AuthApi {

    @Value(value = "${auth.clientId}")
    private String clientId;

    @Value(value = "${auth.clientSecret}")
    private String clientSecret;

    @Value(value = "${auth.cookieDomain}")
    private String cookieDomain;

    @Value(value = "${auth.cookieMaxAge}")
    private int cookieMaxAge;

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping(value = "/userlogin")
    public LoginResult login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        if (StrUtil.isEmpty(username)) {
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }

        String password = loginRequest.getPassword();
        if (StrUtil.isEmpty(password)) {
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        // 根据用户信息和客户端信息，获取身份认证
        Optional<AuthToken> authTokenOptional = this.authService.login(username, password, clientId, clientSecret);
        if (!authTokenOptional.isPresent()) {
            return LoginResult.build(CommonCode.FAIL, null);
        }

        // 取出用户身份令牌，存入cookie
        String access_token = authTokenOptional.get().getAccess_token();
        this.saveCookie(access_token);

        return LoginResult.build(CommonCode.SUCCESS, access_token);
    }

    /**
     * 存储身份令牌到cookie
     * @param access_token
     */
    private void saveCookie(String access_token) {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        if (response != null) {
            CookieUtil.addCookie(response, cookieDomain, "/", "uid", access_token, cookieMaxAge, false);
        }
    }

    @Override
    @PostMapping(value = "/userlogout")
    public ResponseResult logout() {
        // 获取身份令牌
        String uid = this.getTokenFromCookie();
        if (StrUtil.isEmpty(uid)) {
            return new ResponseResult(CommonCode.FAIL);
        }

        // 删除redis中对应的完整令牌信息
        this.authService.delToken(uid);

        // 清除cookie
        this.clearCookie(uid);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 从cookie中获取用户身份令牌
     * @return
     */
    private String getTokenFromCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (StrUtil.isEmpty(map.get("uid"))) {
            return null;
        }
        return map.get("uid");
    }

    /**
     * 清空cookie
     * @param access_token
     */
    private void clearCookie(String access_token) {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        if (response != null) {
            // 将cookie存活时间置为0
            CookieUtil.addCookie(response, cookieDomain, "/", "uid", access_token, 0, false);
        }
    }

    @Override
    @GetMapping(value = "/userjwt")
    public JwtResult userjwt() {
        // 获取身份令牌
        String uid = this.getTokenFromCookie();
        if (StrUtil.isEmpty(uid)) {
            return new JwtResult(CommonCode.FAIL, null);
        }

        // 根据身份令牌从redis获取完整的令牌信息
        Optional<AuthToken> authTokenOptional = this.authService.getUserToken(uid);
        if (!authTokenOptional.isPresent()) {
            return new JwtResult(CommonCode.FAIL, null);
        }

        String jwtToken = authTokenOptional.get().getJwt_token();
        return new JwtResult(CommonCode.SUCCESS, jwtToken);
    }
}
