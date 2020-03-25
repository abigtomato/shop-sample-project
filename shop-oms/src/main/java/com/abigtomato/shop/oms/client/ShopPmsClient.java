package com.abigtomato.shop.oms.client;

import com.abigtomato.shop.api.pms.PmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.PMS_SERVICE)
public interface ShopPmsClient extends PmsApi {
}
