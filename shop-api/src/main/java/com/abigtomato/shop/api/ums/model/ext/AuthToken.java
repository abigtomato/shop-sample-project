package com.abigtomato.shop.api.ums.model.ext;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthToken {

    String access_token;    // 访问token就是短令牌，用户身份令牌
    String refresh_token;   // 刷新token
    String jwt_token;       // jwt令牌
}
