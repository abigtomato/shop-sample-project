package com.abigtomato.shop.order.client;

import com.abigtomato.shop.api.oms.OmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.OMS_SERVICE)
public interface ShopOmsClient extends OmsApi {
}
