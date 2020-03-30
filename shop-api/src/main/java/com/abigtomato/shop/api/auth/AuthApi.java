package com.abigtomato.shop.api.auth;

import com.abigtomato.shop.api.ums.model.request.LoginRequest;
import com.abigtomato.shop.api.ums.model.response.JwtResult;
import com.abigtomato.shop.api.ums.model.response.LoginResult;
import com.abigtomato.shop.core.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "用户认证接口")
public interface AuthApi {

    @ApiOperation(value = "登录")
    @PostMapping(value = "/userlogin")
    LoginResult login(@RequestBody LoginRequest loginRequest);

    @ApiOperation(value = "退出")
    @PostMapping(value = "/userlogout")
    ResponseResult logout();

    @ApiOperation(value = "查询用户jwt令牌")
    @GetMapping(value = "/userjwt")
    JwtResult userjwt();

    @ApiOperation(value = "从cookie中获取身份令牌")
    @GetMapping(value = "/token/from/cookie")
    String getTokenFromCookie(@RequestBody ServerHttpRequest request);

    @ApiOperation(value = "从header中获取jwt令牌")
    @GetMapping(value = "/jwt/from/header")
    String getJwtFromHeader(@RequestBody ServerHttpRequest request);

    @ApiOperation(value = "获取redis中令牌信息的过期时间")
    @GetMapping(value = "/expire")
    long getExpire(@RequestParam(name = "token") String token);
}
