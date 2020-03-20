package com.abigtomato.shop.core.response;

public interface ResultCode {

    boolean success();  // 操作是否成功，true为成功，false操作失败

    int code();         // 操作代码

    String message();   // 提示信息
}
