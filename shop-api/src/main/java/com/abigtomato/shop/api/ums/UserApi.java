package com.abigtomato.shop.api.ums;

import com.abigtomato.shop.api.ums.model.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "用户管理api")
public interface UserApi {

    @ApiOperation(value = "根据账号查询用户信息")
    @GetMapping(value = "/ucenter/getuserext")
    XcUserExt getUserext(@RequestParam("username") String username);
}
