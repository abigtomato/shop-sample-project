package com.abigtomato.shop.auth.client;

import com.abigtomato.shop.api.ums.UserApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.UMS_SERVICE)
public interface UserClient extends UserApi {
}
