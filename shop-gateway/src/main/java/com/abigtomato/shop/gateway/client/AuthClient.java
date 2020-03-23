package com.abigtomato.shop.gateway.client;

import com.abigtomato.shop.api.auth.AuthApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(value = ServiceNameList.AUTH_SERVICE)
public interface AuthClient extends AuthApi {
}
