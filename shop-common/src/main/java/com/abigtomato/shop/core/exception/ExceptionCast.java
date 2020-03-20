package com.abigtomato.shop.core.exception;

import com.abigtomato.shop.core.response.ResultCode;

public class ExceptionCast {

    public static void cast(ResultCode resultCode){
        throw new CustomException(resultCode);
    }
}
