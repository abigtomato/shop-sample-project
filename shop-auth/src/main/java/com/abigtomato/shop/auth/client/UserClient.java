package com.abigtomato.shop.auth.client;

import com.abigtomato.shop.api.ums.UserApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import com.abigtomato.shop.model.ums.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = ServiceNameList.UMS_SERVICE)
public interface UserClient extends UserApi {
}
