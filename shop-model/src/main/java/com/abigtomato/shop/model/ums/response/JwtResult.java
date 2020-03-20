package com.abigtomato.shop.model.ums.response;

import com.abigtomato.shop.core.response.ResponseResult;
import com.abigtomato.shop.core.response.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class JwtResult extends ResponseResult {

    private String jwt;

    public JwtResult(ResultCode resultCode, String jwt) {
        super(resultCode);
        this.jwt = jwt;
    }
}
