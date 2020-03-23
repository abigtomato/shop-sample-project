package com.abigtomato.shop.api.ums.model.response;

import com.abigtomato.shop.core.response.ResponseResult;
import com.abigtomato.shop.core.response.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class LoginResult extends ResponseResult {

    private String token;

    public LoginResult(ResultCode resultCode, String token) {
        super(resultCode);
        this.token = token;
    }

    public static LoginResult build(ResultCode resultCode, String token) {
        return new LoginResult(resultCode, token);
    }
}
