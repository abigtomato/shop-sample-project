package com.abigtomato.shop.oms.client;

import com.abigtomato.shop.api.ums.UmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.UMS_SERVICE)
public interface ShopUmsClient extends UmsApi {
}
