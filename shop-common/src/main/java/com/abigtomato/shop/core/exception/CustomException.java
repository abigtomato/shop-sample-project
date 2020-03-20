package com.abigtomato.shop.core.exception;

import com.abigtomato.shop.core.response.ResultCode;

/**
 * 自定义异常类型
 */
public class CustomException extends RuntimeException {

    ResultCode resultCode;  // 错误代码

    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
