package com.abigtomato.shop.api.auth;

import com.abigtomato.shop.core.response.ResponseResult;
import com.abigtomato.shop.model.ums.request.LoginRequest;
import com.abigtomato.shop.model.ums.response.JwtResult;
import com.abigtomato.shop.model.ums.response.LoginResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
