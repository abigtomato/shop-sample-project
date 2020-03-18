package com.abigtomato.shop.search.feign;

import com.abigtomato.shop.wms.api.ShopWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "wms-service")
public interface ShopWmsClient extends ShopWmsApi {
}
