package com.abigtomato.shop.model.ums.request;

import com.abigtomato.shop.core.request.RequestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class LoginRequest extends RequestData {

    String username;
    String password;
    String verifycode;
}
